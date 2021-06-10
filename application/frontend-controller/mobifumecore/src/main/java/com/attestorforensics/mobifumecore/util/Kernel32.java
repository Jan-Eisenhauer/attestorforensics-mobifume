package com.attestorforensics.mobifumecore.util;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import java.util.ArrayList;
import java.util.List;

/**
 * Copy from https://stackoverflow.com/questions/3434719/how-to-get-the-remaining-battery-life-in
 * -a-windows-system
 */
public interface Kernel32 extends StdCallLibrary {

  Kernel32 instance = Native.loadLibrary("Kernel32", Kernel32.class);

  /**
   * Fill the structure.
   */
  int GetSystemPowerStatus(SystemPowerStatus result);

  /**
   * @see http://msdn2.microsoft.com/en-us/library/aa373232.aspx
   */
  class SystemPowerStatus extends Structure {

    public byte acLineStatus;
    public byte batteryFlag;
    public byte batteryLifePercent;
    public byte reserved1;
    public int batteryLifeTime;
    public int batteryFullLifeTime;

    @Override
    protected List<String> getFieldOrder() {
      ArrayList<String> fields = new ArrayList<String>();
      fields.add("acLineStatus");
      fields.add("batteryFlag");
      fields.add("batteryLifePercent");
      fields.add("reserved1");
      fields.add("batteryLifeTime");
      fields.add("batteryFullLifeTime");
      return fields;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("ACLineStatus: " + getACLineStatusString() + "\n");
      sb.append("Battery Flag: " + getBatteryFlagString() + "\n");
      sb.append("Battery Life: " + getBatteryLifePercent() + "\n");
      sb.append("Battery Left: " + getBatteryLifeTime() + "\n");
      sb.append("Battery Full: " + getBatteryFullLifeTime() + "\n");
      return sb.toString();
    }

    /**
     * The AC power status
     */
    public String getACLineStatusString() {
      switch (acLineStatus) {
        case (0):
          return "Offline";
        case (1):
          return "Online";
        default:
          return "Unknown";
      }
    }

    /**
     * The battery charge status
     */
    public String getBatteryFlagString() {
      switch (batteryFlag) {
        case (1):
          return "High, more than 66 percent";
        case (2):
          return "Low, less than 33 percent";
        case (4):
          return "Critical, less than five percent";
        case (8):
          return "Charging";
        case ((byte) 128):
          return "No system battery";
        default:
          return "Unknown";
      }
    }

    /**
     * The percentage of full battery charge remaining
     */
    public String getBatteryLifePercent() {
      return (batteryLifePercent == (byte) 255) ? "Unknown" : batteryLifePercent + "%";
    }

    /**
     * The number of seconds of battery life remaining
     */
    public String getBatteryLifeTime() {
      return (batteryLifeTime == -1) ? "Unknown" : batteryLifeTime + " seconds";
    }

    /**
     * The number of seconds of battery life when at full charge
     */
    public String getBatteryFullLifeTime() {
      return (batteryFullLifeTime == -1) ? "Unknown" : batteryFullLifeTime + " seconds";
    }
  }
}
