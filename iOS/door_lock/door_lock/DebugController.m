//
//  UIViewController+DebugController.m
//  
//
//  Created by Matthew Rocco on 9/1/15.
//
//

#import "DebugController.h"
#import "Spark-SDK.h"


@interface DebugController ()

@property (weak, nonatomic) IBOutlet UISwitch *switch_1;
@property (weak, nonatomic) IBOutlet UISwitch *switch_2;
@property (weak, nonatomic) IBOutlet UISwitch *switch_3;
@property (weak, nonatomic) IBOutlet UISwitch *switch_4;

@end

// This should be changed to the one in the ViewController or at least moved to AppDelegate.
SparkDevice *photon_dumb;


// Defines a timer to refresh the data on the view. This needs to be invalidated if the view
// loses focus.
NSTimer *refreshTimer;

@implementation DebugController

// Retrieve the state of the switches from the board. Update the screen accordingly.
// These are all ayncronious calls so it is suggested that the motor is in a slow
// turning mode so the Particle API has time to respond.
-(void)update_switches{
    
    [photon_dumb getVariable:@"state_1" completion:^(id result, NSError *error) {
        NSLog(@"result is %@",(NSNumber *)result);
        if (!error) {
            if ([(NSNumber *)result  isEqual: @1])
            {
                [self.switch_1 setOn:YES animated:YES];
            }
            else
                [self.switch_1 setOn:NO animated:YES];
            
        }
        else {
        }
    }];
    
    [photon_dumb getVariable:@"state_2" completion:^(id result, NSError *error) {
        NSLog(@"result is %@",(NSNumber *)result);
        if (!error) {
            if ([(NSNumber *)result  isEqual: @1])
            {
                [self.switch_2 setOn:YES animated:YES];
            }
            else
                [self.switch_2 setOn:NO animated:YES];
        }
        else {
        }
    }];
    
    [photon_dumb getVariable:@"state_3" completion:^(id result, NSError *error) {
        NSLog(@"result is %@",(NSNumber *)result);
        if (!error) {
            if ([(NSNumber *)result  isEqual: @1])
            {
                [self.switch_3 setOn:YES animated:YES];
            }
            else
                [self.switch_3 setOn:NO animated:YES];
        }
        else {
        }
    }];
    
    [photon_dumb getVariable:@"state_4" completion:^(id result, NSError *error) {
        NSLog(@"result is %@",(NSNumber *)result);
        if (!error) {
            if ([(NSNumber *)result  isEqual: @1])
            {
                [self.switch_4 setOn:YES animated:YES];
            }
            else
                [self.switch_4 setOn:NO animated:YES];
        }
        else {
        }
    }];
}



// Initial setup.
- (void)viewDidLoad {
    [super viewDidLoad];
    
    [[SparkCloud sharedInstance] loginWithUser:@"gymboh32@gmail.com" password:@"Ragecastle69" completion:^(NSError *error) {
        if (!error){
            NSLog(@"Logged in to cloud");
            [[SparkCloud sharedInstance] getDevices:^(NSArray *sparkDevices, NSError *error) {
                NSLog(@"%@",sparkDevices.description); // print all devices claimed to user
                
                // Look for out board "kitty_hobo"
                for (SparkDevice *device in sparkDevices)
                {
                    if ([device.name isEqualToString:@"kitty_hobo"])
                        photon_dumb = device;

                    
                }}];
        }
        NSLog(@"failed login");
        }];
    
    // Refresh the switches. If the network is poor, increase this interval and slow down
    // the motor speed.
    refreshTimer = [NSTimer scheduledTimerWithTimeInterval: 0.1
                                             target: self
                                           selector:@selector(update_switches)
                                           userInfo: nil repeats:YES];

    

    
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:true];
    [refreshTimer invalidate];
}

@end
