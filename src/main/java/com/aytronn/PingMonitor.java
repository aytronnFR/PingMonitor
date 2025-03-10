package com.aytronn;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PingMonitor {
  private static final String MONITORED_URL = "https://www.googlee.com"; // URL à surveiller

  public static void main(String[] args) {
    while (true) {
      try {
        boolean isOnline = isWebsiteOnline(MONITORED_URL);

        if (!isOnline) {
          System.out.println("🔴 Ping failed! Restarting USB device...");
          restartUSB();
        } else {
          System.out.println("✅ Ping successful.");
        }

        // Attendre 1 minute avant de refaire le ping
        Thread.sleep(60_000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private static boolean isWebsiteOnline(String url) {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("HEAD"); // Vérifie seulement l'en-tête (plus rapide)
      connection.setConnectTimeout(5000); // Timeout de 5 secondes
      connection.connect();

      int responseCode = connection.getResponseCode();
      return (responseCode >= 200 && responseCode < 400); // 200-399 = OK
    } catch (IOException e) {
      return false; // Site injoignable
    }
  }

  public static void restartUSB() {
    String os = System.getProperty("os.name").toLowerCase();

    System.out.println("ℹ️ OS: " + os);
    if (os.contains("win")) {
      restartUSBWindows();
    } else if (os.contains("nix") || os.contains("nux")) {
      restartUSBUnix();
    } else if (os.contains("mac")) {
      restartUSBMac();
    } else {
      System.out.println("❌ Unsupported OS: " + os);
    }
  }


  private static void restartUSBWindows() {
    String DEVCON_PATH = "C:\\path\\to\\devcon.exe"; // Modifie avec le bon chemin
    String USB_DEVICE_ID = "USB\\VID_XXXX&PID_YYYY"; // Modifie avec l'ID de ton USB

    try {
      System.out.println("🔄 Restarting USB device on Windows...");

      // Désactiver
      ProcessBuilder disable = new ProcessBuilder("cmd.exe", "/c", DEVCON_PATH + " disable \"" + USB_DEVICE_ID + "\"");
      disable.start().waitFor();

      Thread.sleep(3000); // Pause

      // Réactiver
      ProcessBuilder enable = new ProcessBuilder("cmd.exe", "/c", DEVCON_PATH + " enable \"" + USB_DEVICE_ID + "\"");
      enable.start().waitFor();

      System.out.println("✅ USB restarted on Windows.");
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void restartUSBUnix() {
    String USB_DEVICE_ID = "Bus 001 Device 002"; // Modifie avec le bon ID (trouvé via `lsusb`)

    try {
      System.out.println("🔄 Restarting USB device on Linux...");

      // Désactiver + Réactiver avec usbreset
      ProcessBuilder processBuilder = new ProcessBuilder("sudo", "usbreset", USB_DEVICE_ID);
      processBuilder.start().waitFor();

      System.out.println("✅ USB restarted on Linux/Mac.");
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void restartUSBMac() {
    try {
      System.out.println("🔄 Restarting USB device on macOS...");

      // Exécuter les commandes shell pour redémarrer l'USB
      ProcessBuilder unmount = new ProcessBuilder("diskutil", "unmountDisk", "/dev/disk7");
      ProcessBuilder eject = new ProcessBuilder("diskutil", "eject", "/dev/disk7");
      ProcessBuilder stopUsbd = new ProcessBuilder("sudo", "killall", "-STOP", "-c", "usbd");
      ProcessBuilder startUsbd = new ProcessBuilder("sudo", "killall", "-CONT", "-c", "usbd");
      ProcessBuilder mount = new ProcessBuilder("diskutil", "mountDisk", "/dev/disk7");

      // Exécuter les commandes
      unmount.start().waitFor();
      eject.start().waitFor();
      Thread.sleep(3000); // Pause pour libérer le périphérique
      stopUsbd.start().waitFor();
      Thread.sleep(1000);
      startUsbd.start().waitFor();
      Thread.sleep(1000);
      mount.start().waitFor();

      System.out.println("✅ USB restarted on macOS.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
