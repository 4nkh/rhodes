//
//  ptestsViewController.h
//  ptests
//
//  Created by Vlad on 8/25/10.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import <UIKit/UIKit.h>

@class RhoConnectClient, RhomModel;
@interface ptestsViewController : UIViewController {
	IBOutlet UITextView *txtResult;
	IBOutlet UIButton *btnStart;
	IBOutlet UIButton *btnBenchSearch;	
	IBOutlet UIButton *btnBenchCreate;		
	IBOutlet UIButton *btnBenchBulk;			
	IBOutlet UIButton *btnBenchAsyncHttp;				
	IBOutlet UIActivityIndicatorView *indicator;
	
	RhoConnectClient* sclient;
	RhomModel* perftest;
	RhomModel* product;
	RhomModel* customer;
	NSString* result;
	int nCount;
	BOOL tests_initialized;
}

@property (nonatomic, retain) IBOutlet UITextView *txtResult;
@property (nonatomic, retain) IBOutlet UIButton *btnStart;
@property (nonatomic, retain) IBOutlet UIActivityIndicatorView *indicator;

- (IBAction)runTest:(id)sender;
- (IBAction)runBench:(id)sender;

@end

