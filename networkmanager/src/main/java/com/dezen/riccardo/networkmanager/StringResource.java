package com.dezen.riccardo.networkmanager;

import androidx.annotation.Nullable;

//CODE-REVIEW DE ZEN
/**
 * String key and value implementation for Resource interface.
 * @author Riccardo De Zen.
 */
public class StringResource implements Resource<String, String> {

    private String name;
    private String value;

    public StringResource(String name, String value){
        this.name = name;
        this.value = value;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isValid(){
        return this.name != null && !this.name.isEmpty();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof StringResource){
            return ((StringResource) obj).getName().equals(this.name);
        }
        else return false;
    }

    public static StringResource getDefaultInvalid(){
        return new StringResource("","");
    }
}
