BtSMS-en
========

Sending SMS from the tablet through the channel GSM phone using Bluetooth


Work performed to demonstrate skills in application development
for mobile devices running Android.

1. Information.
Application "BtSMS" has been developed in Eclipse on OS Ubuntu 12.04.
Were used:
1) UI elements Button, ImageButton, TextView, TextEdit, ListView, TabHost;
2) bluetooth api to find, connect and exchange information with the remote device;
3) broadcast receivers receiving signals from local bluetooth adapter;
4) threads read, delete, send sms and read the list of contacts;
5) Handler for sending messages from the thread;
6) SQLite for storing read SMS;
7) collection ArrayList.
Testing was conducted on a real device running Android 4.1.1.

2. Features of the application.

- Connecting any phone equipped with Bluetooth interface;
- Reading sms from phone memory and SIM card memory;
- Deleting SMS from phone memory and SIM card memory;
- Display on the screen according to the selected SMS status (read / ( no read) sent / (no sent));
- Sending sms;
- The ability to choose the recipient's number from the phone book of the connected phone.

4. Limitations of the application.
1) brand phones Nokia, do not support reading sms via bluetooth interface
using direct AT-commands;
2) phone brand Nokia, do not support the correct reading of the list of contacts via bluetooth interface
using direct AT-commands;
3) send a short text message (no more than 152 characters);
4) support only Latin characters;
5) does not send SMS via phone with two SIM cards

Developer - Aleksandr V. Karpenko 
e-mail: karpenkoav@ukr.net
