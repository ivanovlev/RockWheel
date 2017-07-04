package lion.rockwheel;

import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Базовый класс окон со вспомогательными методами
 */
public abstract class BasePanel extends AppCompatActivity {
    protected String getViewInfo(Integer viewId){
        TextView view = (TextView)findViewById(viewId);

        if (view instanceof CheckBox){
            return String.valueOf(((CheckBox)view).isChecked());
        }

        return view.getText().toString();
    }

    protected void setViewInfo(Integer viewId, Object info){
        TextView view = (TextView)findViewById(viewId);

        if (view instanceof CheckBox){
            ((CheckBox)view).setChecked((Boolean)info);
            return;
        }

        view.setText(String.valueOf(info));
    }

    protected void showMessage(CharSequence text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
