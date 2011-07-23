//
//  RhomModel.h
//  SyncClientTest
//
//  Created by evgeny vovchenko on 8/23/10.
//  Copyright 2010 RhoMobile. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "RhoConnectNotify.h"

@interface RhomModel : NSObject {
	NSString* name;
	int       sync_type; 
}

@property(assign) NSString* name;
@property int       sync_type;

- (id) init;

- (RhoConnectNotify*) sync;
- (void) sync: (SEL) callback target:(id)target;

- (void) create: (NSMutableDictionary *) data;
- (NSMutableDictionary *) find: (NSString*)object_id;
- (NSMutableDictionary *) find_first: (NSDictionary *)cond;
- (NSMutableArray *) find_all: (NSDictionary *)cond;
- (void) save: (NSDictionary *)data;
- (void) destroy: (NSDictionary *)data;

- (void) startBulkUpdate;
- (void) stopBulkUpdate;

@end
