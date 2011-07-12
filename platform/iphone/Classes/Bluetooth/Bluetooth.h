//
//  Bluetooth.h
//  rhorunner
//
//  Created by Soldatenkov Dmitry on 27/07/10.
//  Copyright 2010 Rhomobile. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <GameKit/GameKit.h>


#define PEER_MODE 1
#define SERVER_MODE 2
#define CLIENT_MODE 3
#define CONNECTED_MODE 5

@interface RhoBluetoothManager : NSObject <GKPeerPickerControllerDelegate, GKSessionDelegate> {
	GKSession		*mysession;

	NSString* connectionCallbackURL; 
	NSString* sessionCallbackURL; 
	
	NSString* deviceName;
	NSString* connectedDeviceName;
	NSString* connectedDeviceID;

	NSMutableArray* packets;
    
    int mode;
    NSString* custom_connect_client_name;
    NSString* custom_connect_server_name;
    
}

@property(nonatomic, retain) GKSession	 *mysession;
@property(nonatomic, copy)	 NSString	 *deviceName;
@property(nonatomic, copy)	 NSString	 *connectedDeviceName;
@property(nonatomic, copy)	 NSString	 *connectedDeviceID;
@property (readwrite, copy) NSString *connectionCallbackURL;
@property (readwrite, copy) NSString *sessionCallbackURL;
@property(nonatomic, retain) NSMutableArray	 *packets;
@property (readwrite, assign) int mode;
@property(nonatomic, retain) NSString* custom_connect_client_name;
@property(nonatomic, retain) NSString* custom_connect_server_name;


+ (RhoBluetoothManager*)sharedInstance;

// Peer Picker Related Methods
- (void)startPicker:(NSString*)callback;

// GKPeerPickerControllerDelegate Methods
- (void)peerPickerControllerDidCancel:(GKPeerPickerController *)picker;
- (GKSession *)peerPickerController:(GKPeerPickerController *)picker sessionForConnectionType:(GKPeerPickerConnectionType)type;
- (void)peerPickerController:(GKPeerPickerController *)picker didConnectPeer:(NSString *)peerID toSession:(GKSession *)session;

// Session Related Methods
- (void)invalidateSession:(GKSession *)msession;

// Data Send/Receive Methods
- (void)receiveData:(NSData *)data fromPeer:(NSString *)peer inSession:(GKSession *)session context:(void *)context;
- (void)sendData:(GKSession *)session withData:(void *)data ofLength:(int)length reliable:(BOOL)howtosend;

// GKSessionDelegate Methods
- (void)session:(GKSession *)session peer:(NSString *)peerID didChangeState:(GKPeerConnectionState)state;


- (void)fireConnectionCallback:(NSString*)status connected_device_name:(NSString*)connected_device_name;
- (void)fireSessionCallback:(NSString*)connected_device_name event_type:(NSString*)event_type;

- (int)readFromPackets:(void*)buf length:(int)length;
- (void)addToPackets:(void*)buf length:(int)length;
- (void)clearPackets;
- (int)getPacketsSize;

- (NSString*)readString;
- (void)sendString:(NSString*)string;

- (void)sendData:(void*)buf length:(int)length;

@end