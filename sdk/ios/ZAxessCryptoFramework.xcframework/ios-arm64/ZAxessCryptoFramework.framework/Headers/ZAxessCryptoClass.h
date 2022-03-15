//
//  ZAxessCryptoClass.h
//  ZAxessCryptoFramework
//
//  Created by Andrea Mancini on 03/08/18.
//  Copyright © 2018 Andrea Mancini. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ZAxessCryptoClass: NSObject

+ (NSData *)CryptingType1: (NSData *)plain_data uuidservice: (NSData *)uuidservice challange: (NSData *)challange key: (NSString *)key;
+ (NSData *)DecryptingType1: (NSData *)plain_data uuidservice: (NSData *)uuidservice challange: (NSData *)challange key: (NSString *)key;

@end
