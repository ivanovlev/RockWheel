package lion.rockwheel;

import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Базовый класс окон со вспомогательными методами
 */
public abstract class BasePanel extends AppCompatActivity {
    protected String getViewText(Integer viewId){
        TextView view = (TextView)findViewById(viewId);
        return view.getText().toString();
    }

    protected void setViewText(Integer viewId, Object text){
        TextView view = (TextView)findViewById(viewId);
        view.setText(String.valueOf(text));
    }

    protected void showMessage(CharSequence text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
