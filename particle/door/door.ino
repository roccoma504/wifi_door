// ---------------------------------------------------------------------------------------------
// WiFi enabled Lockitron door lock powered by Particle.
//
// ----------By matt rocco (roccoma504@gmail.com) and james hall (gymboh32@gmail.com)-----------
//
// ---------------------------------------------------------------------------------------------

// EC -------- Pin ----- Variable
// ---------------------------------------------------------------------------------------------
// LED         D7        led1
// Switch      D1        magnet
// Limit 1     A0        pin_sw_1/state_1    1a 
// Limit 2     A1        pin_sw_2/state_2    2a 
// Limit 3     A2        pin_sw_3/state_3    1b 
// Limit 4     A3        pin_sw_4/state_4    2b 
// ---------------------------------------------------------------------------------------------

// Define the pins for the switches here. The other 4 pins are connected to common ground.
// The configuration is as follows. 
//
// ----- sw_1 ----- sw_3 -----
// ----- sw_2 ----- sw_4 -----
//
// Note that the orientation of the Lockitron should be with the switches at the bottom to correspond with the
// above mapping.
//
int pin_sw_1 = A0;
int pin_sw_2 = A1;
int pin_sw_3 = A2;
int pin_sw_4 = A3;

// Define our motor PWM pin.
int motor_pwm = A6;

// State variables for the limit switches. Used by the software to know if the door is locked/unlocked and by
// the hardware to know where the lock is at any point.
int state_1;
int state_2;
int state_3;
int state_4;

// Variables used by our motor control board. These are used to drive the motor to open/close the lock.
int PWMA = A7; //Speed control 
int AIN1 = D5; //Direction
int AIN2 = D6; //Direction

// Required to drive and stop the motor.
int STBY = D7; //Standby

// Define the single blue LED pin.
int led1 = D7;

// Define the pin the magnet is connected to.
int magnet = D1;

// Define a connected int. This is used by the software to know if the magnet is connected or not.
int connected = 0;

// Set a global fot the board running variable. This is used as a check in the loop function and retrived by the 
// software to see if the board is up. Due to the limitations of the interface, this needs to be a integer.
// 0 - off
// 1 - on
int board_running = 0;

// Defines the lock state. At start we don't know where the lock is so the state is set to -1.
// Locked = 1
// Unlocked = 0
int door_locked = -1;

// Determines if the door should be automatically locked. This occurs if the door has just closed
// from being open.
int auto_lock = 0;

// Motor speed and direction definitions.
const uint8_t MOTOR_SPEED = 200;
const uint8_t MOTOR_CW = 0; // Unlock
const uint8_t MOTOR_CCW = 1; // Lock

void setup() {
  
    // Define our pin I/O.
    pinMode(led1, OUTPUT);
    
    // For the these pins we want the pins to be conneted to pullup resistors so the value is high
    // when the switch is depressed.
    pinMode(magnet, INPUT_PULLUP); 
    pinMode(pin_sw_1, INPUT_PULLUP);
    pinMode(pin_sw_2, INPUT_PULLUP);
    pinMode(pin_sw_3, INPUT_PULLUP);
    pinMode(pin_sw_4, INPUT_PULLUP);
    
    // Set the pins to drive the motor to outputs.
    pinMode(STBY, OUTPUT);
    pinMode(PWMA, OUTPUT);
    pinMode(AIN1, OUTPUT);
    pinMode(AIN2, OUTPUT);
  
    // Set the global variables for software access.
    Spark.variable("magnet", &connected, INT);
    
    // These are all required for debugging via the mobile applcations.
    Spark.variable("board_run", &board_running, INT);
    Spark.variable("state_1", &state_1, INT);
    Spark.variable("state_2", &state_2, INT);
    Spark.variable("state_3", &state_3, INT);
    Spark.variable("state_4", &state_4, INT);
    Spark.variable("door_locked", &door_locked, INT);

    // Set the global functions that are accessed by the mobile applications.
    Spark.function("unlock", unlock);
    Spark.function("lock", lock);
    Spark.function("status", get_status);

    // Set the board on.
    board_running = 1;
    
   // WiFi.off();
}

void loop() 
{
    // Read the state of the magnet.
    read_magnet();
    // Read the state of the switches for debugging purposes.
    read_switches();
    // Check the conditions for the door to auto lock.
    door_auto_lock();
    // Check the state of the door for manual intervention.
    is_locked();
}

// Loop on reading the state of the magnet for display on the software and for
// the hardware to know where the lock is.

void read_magnet () 
{
    connected = digitalRead(magnet);
}

// Reads the states of all limit switches.
void read_switches () 
{
    state_1 = digitalRead(pin_sw_1);
    state_2 = digitalRead(pin_sw_2);
    state_3 = digitalRead(pin_sw_3);
    state_4 = digitalRead(pin_sw_4);
}

// Check to see if the door is locked or unlocked. This is necessary
// as the door can be manually locked and unlocked and we want
// the software appliations to know the state at any given time.
void is_locked()
{
    // Check to see if door is locked.
    if (state_1 == 1 && state_2 == 1 && state_3 == 0 && state_4 == 0)
    {
        door_locked = 1;
    }
    // Check to see if door is unlocked.
    else if (state_1 == 0 && state_2 == 0 && state_3 == 1 && state_4 == 1)
    {
        door_locked = 0;
    }
}



// The reset function that is used by the mobile applications to know what the posistion
// of the lock is for display. To support the Particle API this function needs a String 
// input and Int return type.
// ---------------------------------------------------------------------------------------------
// INPUTS
// command -> Exists only to support the Particle API
// RETURN
// Denotes successful completion of function.
int get_status (String command)
{
    return door_locked;
}

// This function checks the condition of the door just closing from being open. If this
// is the case the door will auto lock.
// ---------------------------------------------------------------------------------------------
void door_auto_lock ()
{
    int finished; 
    
    if (connected && auto_lock == 0)
    {
      auto_lock = 1;
    }
    else if (!connected && auto_lock)
    {
        auto_lock = 0;
        finished = lock("lock");
    }
}

// The reset function that is used by the mobile applications to control the lock.
// This function is used at startup and after each successful turn of the lock to put the
// lock in a known location and to allow the state of the lock to be changed manually.
// ---------------------------------------------------------------------------------------------
// INPUTS
// direction -> Controls the direction of the lock.
// RETURN
// none.
void move_reset (int direction)
{
    do
    {
        move(MOTOR_SPEED, direction);
        read_switches();
    }
    while ( !((state_2 == 1) && (state_4 == 1)));

    digitalWrite(STBY, LOW); 
}

// The unlock function that is used by the mobile applications to control the lock.
// To support the Particle API this function needs a String input and Int return type.
// ---------------------------------------------------------------------------------------------
// INPUTS
// command -> Exists only to support the Particle API.
// RETURN
// Denotes successful completion of function.
int unlock(String command)
{
    if (!connected)
    {
        do
        {
            move(MOTOR_SPEED, MOTOR_CW);
            read_switches();
        }
        while ( !(state_1 == 0 && state_2 == 0 && state_3 == 1 && state_4 == 1));
    
        // Once the door is in the unlocked posistion, wait 100 ms, move the motor
        // back to a reset posistion, put the motor control board in standby and
        // set the state to unlocked.
        delay(100);
        move_reset(MOTOR_CCW);
        digitalWrite(STBY, LOW); 
        door_locked = 0;
    }

    return 1;
}

// The lock function that is used by the mobile applications to control the lock.
// To support the Particle API this function needs a String input and Int return type.
// ---------------------------------------------------------------------------------------------
// INPUTS
// command -> Exists only to support the Particle API.
// RETURN
// Denotes successful completion of function.
int lock(String command)
{
    if (!connected)
    {
        do
        {
            move(MOTOR_SPEED, MOTOR_CCW);
            read_switches();
        }
        while ( !(state_1 == 1 && state_2 == 1 && state_3 == 0 && state_4 == 0));

        // Once the door is in the unlocked posistion, wait 100 ms, move the motor
        // back to a reset posistion, put the motor control board in standby and
        // set the state to unlocked.
        delay(100);
        move_reset(MOTOR_CW);
        digitalWrite(STBY, LOW);
        door_locked = 1;
    }

    return 1;
}

// Move the motor. The unlock, lock and reset methods all use this function.
// ---------------------------------------------------------------------------------------------
// INPUTS
// speed -> Speed at which the motor will move. This needs to be the value of the PWM.
// directon -> The directon of the motor CW = 0 CCW = 1
void move(int speed, int direction)
{
    // Drive the standby pin high. This turns the motor control board on.
    digitalWrite(STBY, HIGH); 
    
    // Set the default outputs (CW) for the pins that control the motors. For the motor to run
    // properly one pin needs to be driven high while one is driven low.
    boolean inPin1 = LOW;
    boolean inPin2 = HIGH;

    if(direction == 1)
    {
        inPin1 = HIGH;
        inPin2 = LOW;
    }

    digitalWrite(AIN1, inPin1);
    digitalWrite(AIN2, inPin2);
    analogWrite(PWMA, speed);
  
}


