# Updater

MOBIfume supports an easy way to update old versions of the software via an usb device.

## Create the updater usb device

1. Format any usb device and set the volume label to `UPDATE` (upper case).
2. Move the appropriate update file to the usb device. The file must keep the name `update.tgz`.

## Use the updater usb device

1. Insert the usb device into the tablet.
2. Start the tablet and the MOBIfume software.
3. Navigate to settings (gear) -> info (i icon).
4. Start the update by clicking on the circle icon and confirming the dialog.

The tablet should restart now.

Note: The circle icon only appears if the usb device and the file is named correctly and if the
update file is valid. If the updater usb device contains the same version as already installed, the circle icon won't appear.
