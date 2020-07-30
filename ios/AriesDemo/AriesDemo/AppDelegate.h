/*
* Copyright SecureKey Technologies Inc. All Rights Reserved.
* SPDX-License-Identifier: Apache-2.0
*/

#import <UIKit/UIKit.h>
#import <CoreData/CoreData.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (readonly, strong) NSPersistentContainer *persistentContainer;

- (void)saveContext;


@end

