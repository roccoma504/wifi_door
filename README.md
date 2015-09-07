WHAT TO BUY
This project was inspired by the following article on Sparkfun.

The link includes important things like what to buy for the actual lock itself. It contains all of the parts we used EXCEPT we switched out the Arduino for a Particle Core.

The core is important as we decided to go with a Wifi based approach rather than a physical button or bluetooth enabled approach. The included iOS and Android software assumes you are running with the particle board.

HOW THINGS WORK
The system has a few main components you should be a aware of.

The lock itself. The lock has 4 limit switches and a spinning motor. The limit switches are read by the micro controller to know where the lock is positioned at any given time.

The micro controller. The Core is a nifty little WiFi controller that acts very similar to an Arduino. The language is Wiring and the form factor is pretty close (just smaller). The .ino file contains the code that the controller runs during execution. Particle offers both a web IDE and a full blown SDK if you so choose.

The mobile applications. The applications use Particle's API to send commands to the board. They are essentially wrappers for the API. The applications have buttons that mimic the physical switches that the spark fun tutorial use.

HOW TO GET THINGS UP
Assuming that you have all of the hardware ready you will need to do a few things before you can get up and running.

Get a Particle login, you'll need it to flash the board.

Find the name of your board. There is a variety of documentation that can help with this.

In the application code swap out your username, password, and board name in the login subprogram.

Assuming you've flashed the board, you should be all set.

FEATURES AND COOL STUFF
DOOR LOCKING AND UNLOCKING

The mobile applications can lock and unlock the door using a button.

VIEW STATE OF LOCK

The mobile applications change background color depending on the current state of the lock.

AUTO LOCK

If the lock detects that the door was recently opened and then shut, the lock will engage and automatically lock the door.

MAGNET DETECTION

If the door senses it is open using the magnet, the lock will not engage.
