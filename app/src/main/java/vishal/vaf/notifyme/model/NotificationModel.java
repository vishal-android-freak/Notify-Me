/**
 * MIT License
 *
 * Copyright (c) 2017 Vishal Dubey
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

package vishal.vaf.notifyme.model;

import android.app.PendingIntent;
import android.app.RemoteInput;
import android.os.Bundle;

/**
 * Created by vishal on 12/01/17.
 */

public class NotificationModel {

    private Bundle bundle;
    private PendingIntent pendingIntent;
    private RemoteInput[] remoteInputs;

    public NotificationModel(Bundle bundle, PendingIntent pendingIntent, RemoteInput[] remoteInputs) {
        this.bundle = bundle;
        this.pendingIntent = pendingIntent;
        this.remoteInputs = remoteInputs;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public RemoteInput[] getRemoteInputs() {
        return remoteInputs;
    }

}
