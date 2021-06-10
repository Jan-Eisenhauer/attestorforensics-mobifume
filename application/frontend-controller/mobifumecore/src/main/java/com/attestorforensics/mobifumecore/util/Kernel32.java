package com.attestorforensics.mobifumecore.util;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Copy from https://stackoverflow.com/questions/3434719/how-to-get-the-remaining-battery-life-in
 * -a-windows-system
 */
public interface Kernel32 extends StdCallLibrary {

  Kernel32 INSTANCE = Native.loadLibrary("Kernel32", Kernel32.class);

  /**
   * Fill the structure.
   */
  // Method must match the native library name
  int GetSystemPowerStatus(SystemPowerStatus result);

  /**
   * @see http://msdn2.microsoft.com/en-us/library/aa373232.aspx
   */
  class SystemPowerStatus extends Structure {

    // Fields must be public to be set by the native library
    public byte acLineStatus;
    public byte batteryFlag;
    public byte batteryLifePercent;
    public byte reserved1;
    public int batteryLifeTime;
    public int batteryFullLifeTime;

    /**
     * The percentage of full battery charge remaining
     */
    public String getBatteryLifePercent() {
      return (batteryLifePercent == (byte) 255) ? "-" : batteryLifePercent + "%";
    }

    @Override
    protected List<String> getFieldOrder() {
      ArrayList<String> fields = new ArrayList<>();
      fields.add("acLineStatus");
      fields.add("batteryFlag");
      fields.add("batteryLifePercent");
      fields.add("reserved1");
      fields.add("batteryLifeTime");
      fields.add("batteryFullLifeTime");
      return fields;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      if (!super.equals(o)) {
        return false;
      }

      SystemPowerStatus that = (SystemPowerStatus) o;
      return acLineStatus == that.acLineStatus && batteryFlag == that.batteryFlag
          && batteryLifePercent == that.batteryLifePercent && reserved1 == that.reserved1
          && batteryLifeTime == that.batteryLifeTime
          && batteryFullLifeTime == that.batteryFullLifeTime;
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), acLineStatus, batteryFlag, batteryLifePercent,
          reserved1, batteryLifeTime, batteryFullLifeTime);
    }
  }
}
