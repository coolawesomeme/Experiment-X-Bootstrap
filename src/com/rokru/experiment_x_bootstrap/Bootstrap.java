package com.rokru.experiment_x_bootstrap;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Bootstrap {

	private static long timer = System.currentTimeMillis();
	private static JFrame splash;
	private static String version = "1.0.0";
	private static URL update_file;
	private static URL download_url;
	private static String latest_version = "0.0.1";
	private static ArrayList<String> arguments = new ArrayList<String>();
	
	public static void main(String[] args) {
		for(String s : args){
			arguments.add(s);
		}if(arguments.contains("-v") || arguments.contains("-version")){
			returnVersionPlain();
			System.exit(0);
		}
		System.out.println("---------------------------------------------");
		System.out.println("[BOOTSTRAP] Bootstrap initializing...");
		
		splashscreen();
		createBootstrapPathFile();
		
		File f = new File(getLauncherDirectory() + "/truelauncher");
		if(f.mkdirs()){
			System.out.println("[BOOTSTRAP] Directory made.");
		}
		checkForLauncherUpdate();
	}

	private static void returnVersionPlain() {
		File launcher_path = new File(getLauncherDirectory() + "/launcher_path.loc");
		String currentVersion = "0.0.0";
		if(launcher_path.exists()){
			try{
			URL lastpath = launcher_path.toURI().toURL();
			Scanner a = new Scanner(lastpath.openStream());
			String pathraw = a.nextLine();
			if (pathraw.contains("|")) {
				String[] info = pathraw.split("\\|", -1);
				currentVersion = info[2];
			}
			a.close();
			}catch(Exception e){}
		}
		System.out.println(currentVersion);
	}

	private static void splashscreen() {
		splash = new JFrame();
		JLabel logo = new JLabel(new ImageIcon(Bootstrap.class.getResource("/images/ex_x_logo.png")));
		logo.setBounds(0, 0, 600, 200);
		splash.setSize(logo.getSize());
		splash.getContentPane().add(logo);
		splash.setUndecorated(true);
		splash.setAlwaysOnTop(true);
		splash.setLocationRelativeTo(null);
		splash.setBackground(new Color(0, 0, 0, 0.5f));
		splash.setIconImage(new ImageIcon(Bootstrap.class.getResource("/images/app_icon.png")).getImage());
		splash.setVisible(true);
		
		System.out.println("[BOOTSTRAP] Splashscreen initialized");
	}

	private static void checkForLauncherUpdate(){
		try {
			update_file = new URL("https://raw.githubusercontent.com/coolawesomeme/Experiment-X-Launcher/master/UPDATE.txt");
			Scanner s = new Scanner(update_file.openStream());
			String raw = s.nextLine();
			if (raw.contains("|")) {
				String[] info = raw.split("\\|", -1);
				latest_version = info[0];
				download_url = new URL(info[1]);
				System.out.println("[BOOTSTRAP] Latest version: " + latest_version);
				System.out.println("[BOOTSTRAP] Download URL: " + download_url.toString());
			}
			s.close();
			File launcher_path = new File(getLauncherDirectory() + "/launcher_path.loc");
			String currentVersion = "0.0.0";
			if(launcher_path.exists()){
				URL lastpath = launcher_path.toURI().toURL();
				Scanner a = new Scanner(lastpath.openStream());
				String pathraw = a.nextLine();
				if (pathraw.contains("|")) {
					String[] info = pathraw.split("\\|", -1);
					currentVersion = info[2];
					System.out.println("[BOOTSTRAP] Current version: " + currentVersion);
				}
				a.close();
			}
			String[] vCurrent = currentVersion.split("\\.");
			String[] vChecked = latest_version.split("\\.");
			if(isOutdated(Integer.parseInt(vCurrent[0]), Integer.parseInt(vCurrent[1]), Integer.parseInt(vCurrent[2]), Integer.parseInt(vChecked[0]), Integer.parseInt(vChecked[1]), Integer.parseInt(vChecked[2]))){
				System.out.println("[BOOTSTRAP] Update found.");
				downloadUpdate();
			}else{
				System.out.println("[BOOTSTRAP] Up to date.");
				startLauncher();
			}
		} catch (Exception e) {
			e.printStackTrace();
			startLauncher();
		}
	}

	private static void downloadUpdate() {
		long downloadTimer = System.currentTimeMillis();
		URLConnection conn;
		try {
			conn = download_url.openConnection();
			InputStream in = conn.getInputStream();
    		FileOutputStream out = new FileOutputStream(getLauncherDirectory() + "/truelauncher/ExperimentXLauncher.jar");
    		byte[] b = new byte[1024];
    		int count;
    		System.out.println("[BOOTSTRAP] Downloading...");
    		while ((count = in.read(b)) >= 0) {
        		out.write(b, 0, count);
    		}
    		System.out.println("[BOOTSTRAP] Jar downloaded in " + (System.currentTimeMillis() - downloadTimer) + " ms.");
    		out.flush(); out.close(); in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startLauncher();
	}

	private static void startLauncher() {
		File f = new File(getLauncherDirectory() + "/truelauncher/ExperimentXLauncher.jar");
		try {
			if(f.exists()){
				String s = "";
				for(int i = 0; i < arguments.size(); i++){
					if(i == 0){
						s = arguments.get(i);
					}else{
						s = s + " " + arguments.get(i);
					}
				}

				ProcessBuilder pb = new ProcessBuilder("java", "-jar", f.getAbsolutePath(), s);
				pb.redirectErrorStream(true);
				splash.setAlwaysOnTop(false);
				Process proc = pb.start();
				
				if(splash != null)
					splash.dispose();
				
				System.out.println("[BOOTSTRAP] Launcher jar started.");
				System.out.println("[BOOTSTRAP] " + (System.currentTimeMillis() - timer) + " ms passed since bootstrap start.");
				
				if(!arguments.isEmpty()){
					System.out.println("[BOOTSTRAP] Argument(s): " + s);
				}				
				System.out.println("---------------------------------------------");
				
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);

				String line;
				int exit = -1;

				while ((line = br.readLine()) != null) {
				    // Outputs your process execution
				    System.out.println("[LAUNCHER] " + line);
				}if((line = br.readLine()) == null){
					try {
				        exit = proc.exitValue();
				        proc.destroy();
				        if (exit == 0)  {
				        	System.out.println("---------------------------------------------");
				            System.out.println("[BOOTSTRAP] Bootstrap Shell Exiting...");
				            Thread.sleep(5000);
				            System.exit(0);
				        }else{
				        	System.out.println("---------------------------------------------");
				        	System.err.println("[BOOTSTRAP] Error! Check true launcher at:");
				        	System.err.println(f.getAbsolutePath());
				        	System.exit(1);
				        }
				    } catch (IllegalThreadStateException t) {}
				}
			}else{
				System.err.println("[BOOTSTRAP] True Launcher not found. Queuing download.");
				downloadUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static String getLauncherDirectory(){
		if(System.getProperty("user.home") != null){
			return System.getProperty("user.home") + "/.experimentx/launcher";
		}else{
			return ".experimentx/launcher";
		}
	}
	
	private static void createBootstrapPathFile(){
		try {
			File q = new File(getLauncherDirectory() + "/bootstrap_path.loc");
			q.createNewFile();
			FileWriter fwrite = new FileWriter(q);
			fwrite.write( JarPath.determineJarFolder() + "|" + JarPath.determineJarPath() + "|" + version);
			System.out.println("[BOOTSTRAP] Bootstrap Folder: " + JarPath.determineJarFolder());
			System.out.println("[BOOTSTRAP] Bootstrap Path: " + JarPath.determineJarPath());
			fwrite.flush();
			fwrite.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isOutdated(int vMajor1, int vMinor1, int vRevision1, int vMajor2, int vMinor2, int vRevision2) {
		if (vMajor1 < vMajor2) {
			return true;
		} else if (vMajor1 > vMajor2) {
			return false;
		} else {
			if (vMinor1 < vMinor2) {
				return true;
			} else if (vMinor1 > vMinor2) {
				return false;
			} else {
				if (vRevision1 < vRevision2) {
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
