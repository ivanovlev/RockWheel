package lion.rockwheel.model;

import com.orm.SugarRecord;

/**
 * Класс опций настроек
 * */
public class ConfigInfo extends SugarRecord<ConfigInfo> {
    // Название переменной
    String option;

    // Значение переменной
    String value;

    // Тип переменной
    String type;

    /**
     * Конструктор по умолчанию (для sql lite)
     */
    public ConfigInfo() {

    }

    /**
     * Конструктор из параметров
     * @param option название переменной
     * @param value значение переменной
     */
    public ConfigInfo (String option, Object value) {
        this.option = option;
        setValue(value);
    }

    public void setValue(Object value){
        this.value = value.toString();
        this.type = value.getClass().getName();
    }

    public Object getValue() {
        if (Float.class.getName().equals(type)){
            return Float.parseFloat(value);
        }

        if (Integer.class.getName().equals(type)){
            return Integer.parseInt(value);
        }

        return value;
    }

    public String getOption(){
        return option;
    }
}
