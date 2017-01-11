/**
 MIT License

 Copyright (c) 2017 Vishal Dubey

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.

 **/


package vishal.vaf.notifyme.activitites;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import vishal.vaf.notifyme.R;

public class CustomizationActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private TextView assistBotText;
    private SharedPreferences.Editor editor;

    private String packageName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customization);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        packageName = getIntent().getStringExtra("package");

        setTitle("Customize " + packageName);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        assistBotText = (TextView) findViewById(R.id.assist_msg);
        if (packageName.equals("WhatsApp"))
            assistBotText.setText(sharedPreferences.getString("assist_wa_msg", "Put your message here"));
        else if (packageName.equals("FBMessenger"))
            assistBotText.setText(sharedPreferences.getString("assist_fb_msg", "Put your message here"));

            findViewById(R.id.edit_assist_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlertDialog();
                }
            });

    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        LayoutInflater inflater = getLayoutInflater();

        View headerView = inflater.inflate(R.layout.assist_dialog_header, null);
        builder.setCustomTitle(headerView);

        View dialogBody = inflater.inflate(R.layout.assist_dialog_msg, null);
        builder.setView(dialogBody);

        final EditText assistMsg = (EditText) dialogBody.findViewById(R.id.assist_msg);
        if (packageName.equals("WhatsApp"))
            assistMsg.setText(assistBotText.getText().toString());
        else if (packageName.equals("FBMessenger"))
            assistMsg.setText(assistBotText.getText().toString());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (packageName.equals("WhatsApp"))
                    editor.putString("assist_wa_msg", assistMsg.getText().toString());
                else if (packageName.equals("FBMessenger"))
                    editor.putString("assist_fb_msg", assistMsg.getText().toString());

                editor.apply();
                assistBotText.setText(assistMsg.getText().toString());
            }
        });

        builder.create();
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
