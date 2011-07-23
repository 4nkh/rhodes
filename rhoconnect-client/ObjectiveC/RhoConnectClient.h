//
//  RhoConnectClient.h
//  SyncClientTest
//
//  Created by evgeny vovchenko on 8/23/10.
//  Copyright 2010 RhoMobile. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RhomModel.h"
#import "RhoConnectNotify.h"
#import "RhoConnectObjectNotify.h"
#include "SyncClient/SyncClient.h"

@interface RhoConnectClient : NSObject {
}

@property(setter=setThreadedMode:) BOOL threaded_mode;
@property(setter=setPollInterval:) int  poll_interval;
@property(assign, setter=setSyncServer:) NSString* sync_server;
@property(setter=setBulkSyncState:, getter=getBulkSyncState) int bulksync_state;

+ (void) initDatabase;
+ (void) setNotification: (SEL) callback target:(id)target;

- (void) setObjectNotification: (SEL) callback target:(id)target;
- (void) clearObjectNotification;
- (void) addObjectNotify: (int) nSrcID szObject:(NSString*) szObject;

- (id) init;
- (void)dealloc;

- (void) addModels:(NSArray*)models;
- (void) database_full_reset_and_logout;
- (BOOL) is_logged_in;

- (RhoConnectNotify*) loginWithUser: (NSString*) user pwd:(NSString*) pwd;
- (void) loginWithUser: (NSString*) user pwd:(NSString*) pwd callback:(SEL) callback target:(id)target;
- (void) setNotification: (SEL) callback target:(id)target;
- (void) clearNotification;

- (RhoConnectNotify*) syncAll;

- (RhoConnectNotify*) search: (NSArray*)models from: (NSString*) from params: (NSString*)params sync_changes: (BOOL) sync_changes progress_step: (int) progress_step;

- (void) setConfigString: (NSString*)name param: (NSString*) param;
- (NSString*) getConfigString: (NSString*)name;

@end
