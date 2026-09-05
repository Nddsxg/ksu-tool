package com.ksu.tool;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private TextView log;
    private Button btnEx, btnSt;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 40);
        root.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("KSU 临时提权工具");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        btnEx = mkBtn("🔓 自动提权", 0xFFC0392B);
        btnSt = mkBtn("🔒 稳定权限", 0xFF27AE60);
        root.addView(btnEx);
        root.addView(btnSt);

        log = new TextView(this);
        log.setTextColor(0xFF00FF88);
        log.setTypeface(Typeface.MONOSPACE);
        log.setTextSize(11);
        log.setText("点击按钮开始…");
        ScrollView sv = new ScrollView(this);
        sv.addView(log);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);

        btnEx.setOnClickListener(v -> run("exploit"));
        btnSt.setOnClickListener(v -> run("stabilize"));
    }

    private Button mkBtn(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(18);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(color);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 30, 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    private void setBusy(boolean busy) {
        btnEx.setEnabled(!busy);
        btnSt.setEnabled(!busy);
    }

    private void run(final String mode) {
        setBusy(true);
        log.setText("执行中，请勿锁屏…\n");
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                String script = "/sdcard/Download/ksu_tool/" + mode + ".sh";
                // 优先 su，失败退回 sh
                String[] cmd = {"sh", "-c", "su -c 'sh " + script + "' 2>&1 || sh " + script + " 2>&1"};
                Process p = Runtime.getRuntime().exec(cmd);
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append((char)10);
                }
                p.waitFor();
                sb.append((char)10).append("退出码: ".concat(String.valueOf(p.exitValue())));
            } catch (Exception e) {
                sb.append("错误: ").append(e);
            }
            String out = sb.toString();
            runOnUiThread(() -> {
                log.setText(out);
                setBusy(false);
            });
        }).start();
    }
}
