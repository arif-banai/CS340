package me.arifbanai.project1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class QCShell {

	// Used for implementing 'cd' in executeInput(String[] commands)
	// Initially, it is set to the path of the working directory for QCShell
	private static File absolutePath = new File(System.getProperty("user.dir"));

	public static void main(String[] args) {

		String username = "";
		String password = "";

		Scanner input = new Scanner(System.in);

		// Login loop
		boolean login = false;

		while (!login) {
			System.out.println("Please enter your username & password.");
			
			System.out.print("Username: ");
			username = input.nextLine();

			System.out.print("Password: ");
			password = input.nextLine();

			if (!password.equals("cs340")) {
				System.err.println("Wrong password. Try again.");
			} else {
				login = true;
				System.out.println("Login successful.");
			}
		}

		boolean active = true;

		//Prompt and command input loop
		while (active) {
			String currentInput = "";
			System.out.print(username + "> ");

			// The try-catch catches the error caused when using Ctrl+D
			try {
				currentInput = input.nextLine().trim();
			} catch (Exception e) {
				System.out.println();
				System.out.println("Ctrl+D detected: Program terminated");
				System.exit(0);
			}

			//If input is over 512 characters, print an error and continue
			if (currentInput.length() > 512) {
				System.err.println("Error: input over 512 characters");
				continue;
			}

			//Split the input using (;) as delimiter
			String[] commands = currentInput.split(";");

			// Execute commands, and if it returns true, the quit command was used, so exit the shell
			if (executeInput(commands)) {
				input.close();
				System.exit(0);
			}

		}

	}

	public static boolean executeInput(String[] commands) {

		//This boolean is true only when 'quit' command is used
		boolean quitDetected = false;

		for (String x : commands) {

			//Remove leading and trailing whitespace
			x = x.trim();

			//Continue if there is no command
			if (x.isEmpty()) {
				continue;
			}

			//Check if the command is quit, if so, set quitDetected to true
			if (x.equalsIgnoreCase("quit")) {
				quitDetected = true;
				continue;
			}

			// Implement the functionality of 'cd'
			if (x.equals("cd")) {
				absolutePath = new File(System.getProperty("user.dir"));
				continue;
			}

			if (x.startsWith("cd ")) {
				String[] arguments = x.split("\\s+");
				
				if (arguments.length == 0) {
					absolutePath = new File(System.getProperty("user.dir"));
					continue;
				} else if (arguments[1].equals("..")) {
					// If the argument is equal to the string above, parent directory becomes new
					// absolutePath, if it exists
					if (absolutePath.getParentFile() != null) {
						absolutePath = absolutePath.getParentFile().getAbsoluteFile();
					}
					
					continue;
					
				} else {
					// Otherwise, an absolute path may have been, so verify if an absolute pathname
					// was given
					File file = new File(arguments[1]);

					if (file.isAbsolute() && file.isDirectory()) {
						absolutePath = file;
						continue;
					} else {
						// Otherwise it could have been a relative pathname.
						file = new File(absolutePath.getAbsolutePath() + "/" + arguments[1]);

						if (file.isDirectory()) {
							absolutePath = file;
							continue;
						} else {
							//If the argument is not any of the above, it is not a proper pathname
							System.err.println("cd: " + arguments[1] + ": no such file or directory");
							continue;
						}

					}
				}
			}

			//Prepare StringBuffers for final output
			StringBuffer normalOutput = new StringBuffer();
			StringBuffer errorOutput = new StringBuffer();

			try {

				Process p = Runtime.getRuntime().exec(x, null, absolutePath);
				p.waitFor(10, TimeUnit.SECONDS);

				//Prepare BufferedReaders for p.getInputStream() and p.getErrorStream()
				BufferedReader processOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader processErrors = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				//Append the lines from the BufferedReader(s) into the StringBuffer(s)
				String line = "";
				while (((line = processOutput.readLine()) != null)) {
					normalOutput.append(line + "\n");
				}

				line = "";
				while (((line = processErrors.readLine()) != null)) {
					errorOutput.append(line + "\n");
				}

				//If either of the two StringBuffer(s) have anything to output, print them.
				if (normalOutput.length() > 0) {
					System.out.println(normalOutput.toString());
				}
				if (errorOutput.length() > 0) {
					System.err.println(errorOutput.toString());
				}

			} catch (IOException e) {
				System.err.println(x + ": Invalid command!\n");
				continue;
			} catch (InterruptedException e) {
				System.err.println("A thread was interrupted!\n");
				e.printStackTrace();
			}

		}

		//If this is true, the shell will terminate. If it is false, execution will continue as normal
		return quitDetected;
	}

}
