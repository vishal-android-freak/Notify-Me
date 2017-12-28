//
//  ViewController.swift
//  Notify Me
//
//  Created by Vishal Dubey on 13/01/17.
//  Copyright © 2017 Vishal Dubey. All rights reserved.
//

import Cocoa
import FirebaseDatabase

class ViewController: NSViewController, NSUserNotificationCenterDelegate {
    
    var center: NSUserNotificationCenter!
    var notifText: String!
    var ref: DatabaseReference!
    var msgListener: DatabaseHandle!
    var notificationRemoveListener: DatabaseHandle!
    var notifData = [String: NSUserNotification]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        center = NSUserNotificationCenter.default
        center.delegate = self
        ref = Database.database().reference()
        
        msgListener = ref.child("notifyme/123456789/app").observe(DataEventType.value, with: {
            snapshot in
            let data = snapshot.value as? [String: String]
            if (data != nil) {
                let appName = data!["app_name"]
                self.showNotification(title:  appName == "com.whatsapp" ? "WhatsApp":"Messenger", subTitle: data!["name"]!, message: data!["message"]!, id: data!["id"]!, appName: appName!)
            }
            self.ref.child("notifyme/123456789/app").setValue(nil)
        })
        notificationRemoveListener = ref.child("notifyme/123456789/remove").observe(DataEventType.value, with: {
            snapshot in
            let data = snapshot.value as? [String: String]
            if (data != nil) {
                for noti in (self.center?.deliveredNotifications)! {
                    if noti.identifier! == data!["id"]! {
                        self.center?.removeDeliveredNotification(noti)
                        break
                    }
                }
            }
        })
    }
    
    func showNotification(title: String, subTitle: String, message: String, id: String, appName: String) {
        print(id)
        var notification = notifData[id]
        if (notification != nil) {
            print("not nil")
            var text = notification?.informativeText
            if (!(text == message)){
                text = text! + ". " + message
                notification?.informativeText = text
                notifData[id] = notification
            }
        } else {
            print("nil")
            notification = NSUserNotification()
            notification?.title = title
            notification?.subtitle = subTitle
            notification?.identifier = id
            notification?.informativeText = message
            notification?.soundName = NSUserNotificationDefaultSoundName
            notification?.userInfo = ["app_name": appName]
            notification?.hasReplyButton = true
            notification?.otherButtonTitle = "Dismiss"
            notification?.setValue(NSImage(named: NSImage.Name(rawValue: appName == "com.whatsapp" ? "WhatsApp":"Messenger")), forKey: "_identityImage")
            notifData[id] = notification
        }
        center?.deliver(notification!)
    }
    
    func userNotificationCenter(_ center: NSUserNotificationCenter, shouldPresent notification: NSUserNotification) -> Bool {
        return true
    }
    
    func userNotificationCenter(_ center: NSUserNotificationCenter, didActivate notification: NSUserNotification) {
        if notification.activationType == NSUserNotification.ActivationType.replied {
            var dict = notification.userInfo as! [String:String]
            var response = [String:String]()
            response["name"] = notification.subtitle
            response["message"] = notification.response?.string
            response["app_name"] = dict["app_name"]
            response["id"] = notification.identifier
            
            ref.child("notifyme/123456789/desktop").setValue(response)
            notifData.removeValue(forKey: notification.identifier!)
        }
    }
    
    func userNotificationCenter(_ center: NSUserNotificationCenter, didDeliver notification: NSUserNotification) {
        
    }
    
    override func viewDidDisappear() {
        ref.removeObserver(withHandle: msgListener)
        ref.removeObserver(withHandle: notificationRemoveListener)
    }
    
}

