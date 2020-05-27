package com.kylin.upms.biz.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class DateEditor extends PropertyEditorSupport {
    private DateFormat dateFormat;
    private boolean allowEmpty;
    private final int exactDateLength;

    public DateEditor(DateFormat dateFormat, boolean allowEmpty) {
        this.dateFormat = dateFormat;
        this.allowEmpty = allowEmpty;
        this.exactDateLength = -1;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isBlank(text)) {
            this.setValue((Object) null);
        } else if (this.allowEmpty) {
            this.setValue((Object) null);
        } else {
            if (text != null && this.exactDateLength >= 0 && text.length() != this.exactDateLength) {
                throw new IllegalArgumentException("Could not parse date: it is not exactly" + this.exactDateLength + "characters long");
            }
            if (NumberUtils.isNumber(text)) {
                this.setValue(new Date(Long.valueOf(text)));
            } else {
                if (text != null && text.length() == 8) {//如果只有时间部分,则将日期默认为初始日期
                    text = "1970-01-01 " + text;
                } else if (text != null && text.length() == 10) {
                    text = text + " 00:00";
                }
                try {
                    this.setValue(this.dateFormat.parse(text));
                } catch (ParseException var3) {
                    throw new IllegalArgumentException("Could not parse date: " + var3.getMessage(), var3);
                }
            }

        }

    }

    @Override
    public String getAsText() {
        Date value = (Date) this.getValue();
        return value != null ? this.dateFormat.format(value) : "";
    }
}
