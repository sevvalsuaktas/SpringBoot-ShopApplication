package com.example.shop.shop.logging;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // Yalnızca metotlarda kullanılabilir
@Retention(RetentionPolicy.RUNTIME) // Çalışma zamanında da erişilebilir olsun
@Documented
public @interface Loggable {
}
