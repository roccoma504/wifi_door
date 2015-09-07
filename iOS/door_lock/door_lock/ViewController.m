//
//  ViewController.m
//  door_lock
//
//  Created by Matthew Rocco on 8/23/15.
//  Copyright © 2015 Matthew Rocco. All rights reserved.
//

#import "ViewController.h"
#import "Spark-SDK.h"

@interface ViewController ()

// Define an activity view to show the user that thing are happening.
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *activity_view;
@property (weak, nonatomic) IBOutlet UIButton *lock_button;
@property (weak, nonatomic) IBOutlet UIButton *unlock_button;

@end

// Spark device object. Retrieved on load.

// Global timer.
NSTimer *timer;

// Default timeout in iterations.
int timeout_count = 0;

@implementation ViewController


// Retrieves the status of the door and changes the background color.
-(void)door_to_string: (NSNumber*) status
{
    
    // Door is locked.
    if ([status  isEqual: @1])
    {

        self.activity_view.stopAnimating;
        self.view.backgroundColor = [[UIColor alloc]initWithRed:230.0/255.0 green:126.0/255.0 blue:34.0/255.0 alpha:1.0];
        self.lock_button.hidden = YES;
        self.unlock_button.hidden = NO;

    }
    // Door is unlocked.
    else if ([status  isEqual: @0])
    {
        self.activity_view.stopAnimating;
        self.view.backgroundColor = [[UIColor alloc]initWithRed:33.0/255.0 green:150.0/255.0 blue:243.0/255.0 alpha:1.0];
        self.lock_button.hidden = NO;
        self.unlock_button.hidden = YES;
    }
}


// Retrieve the value of the magnet from the board. and set the text field accordingly.
-(void)pin_status{
    [photon getVariable:@"door_locked" completion:^(id result, NSError *error) {
        if (!error) {
            self.activity_view.stopAnimating;
            [self door_to_string:(NSNumber *)result];
            
        }
        else {
            NSLog(@"Failed reading pin");
        }
    }];
}

// Retrieve if the board is connected to Wifi and has successfully booted up.
-(void)board_status{


    [photon getVariable:@"board_run" completion:^(id result, NSError *error) {
        NSLog(@"result is %@",(NSNumber *)result);
        if (!error) {
            if ([(NSNumber *)result  isEqual: @1])
            {
                timeout_count = 0;
                [NSTimer scheduledTimerWithTimeInterval: 0.5 target: self selector:@selector(pin_status)  userInfo: nil repeats:YES];
            }
        }
        else {
            if (timeout_count < 2)
            {
                NSLog(@"Failed reading pin");
                [NSTimer scheduledTimerWithTimeInterval: 0.5 target: self selector:@selector(board_status) userInfo: nil repeats:NO];
            }
            else
                self.activity_view.stopAnimating;
        }
    }];
}

// Login to the Particle cloud. This is required to retrieve the boards set to the account.
-(void)login{
    
    // Do any additional setup after loading the view, typically from a nib.
    
    // Log into the Spark environment. Hardcoded values for now. If the login in successful print out to the
    // screen that we have connected.
    [[SparkCloud sharedInstance] loginWithUser:@"yourname" password:@"yourpassword" completion:^(NSError *error) {
        if (!error){
            NSLog(@"Logged in to cloud");
            [[SparkCloud sharedInstance] getDevices:^(NSArray *sparkDevices, NSError *error) {
                NSLog(@"%@",sparkDevices.description); // print all devices claimed to user
                
                // Look for out board "kitty_hobo"
                for (SparkDevice *device in sparkDevices)
                {
                    if ([device.name isEqualToString:@"yourboard"]) //Put in your device name here.
                        photon = device;
                    [self board_status];
                    
                }}];
        }
        else
        {

        }}];
}

// Start a timer repeating every 3 seconds to attempt to connect to the board.
-(void)login_fail{
    timeout_count = timeout_count + 1;
    timer = [NSTimer scheduledTimerWithTimeInterval: 0.2
                                             target: self
                                           selector:@selector(login)
                                           userInfo: nil repeats:NO];
}


// Initial setup.
- (void)viewDidLoad {
    [super viewDidLoad];

}

-(void)viewDidAppear:(BOOL)animated{

    [super viewDidAppear:true];
    
    // Hide labels on boot.
    self.activity_view.startAnimating;
    self.view.backgroundColor = [[UIColor alloc]initWithRed:236.0/255.0 green:240.0/255.0 blue:241.0/255.0 alpha:1.0];
    [self login];
}


- (IBAction)lock:(id)sender {
    [photon callFunction:@"lock" withArguments:@[@"lock"] completion:^(NSNumber *resultCode, NSError *error) {
        
        if (!error)
        {
            self.activity_view.startAnimating;
        }
        
    }];
}
- (IBAction)unlock:(id)sender {
    [photon callFunction:@"unlock" withArguments:@[@"unlock"] completion:^(NSNumber *resultCode, NSError *error) {
        
        if (!error)
        {
            self.activity_view.startAnimating;
        }

    }];
}

- (IBAction)reset:(id)sender {
    [photon callFunction:@"reset" withArguments:@[@"reset"] completion:^(NSNumber *resultCode, NSError *error) {
        
    }];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
