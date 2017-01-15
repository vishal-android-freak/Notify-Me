//
//  NotificationModel.swift
//  Notify Me
//
//  Created by Vishal Dubey on 15/01/17.
//  Copyright © 2017 Vishal Dubey. All rights reserved.
//

import Foundation

class NotificationModel {
    
    var infoText: String?
    
    init(infoText: String) {
        self.infoText = infoText
    }
    
    func getInfoText() -> String {
        return infoText!
    }
    
}
