package dn.jasm.configuration.swagger.shop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "200", description = "Список магазинов получен или обновлен", content = @Content)
@ApiResponse(responseCode = "201", description = "Список магазинов создан", content = @Content)
@ApiResponse(responseCode = "204", description = "Список магазинов удален", content = @Content)
@ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content)
@ApiResponse(responseCode = "404", description = "Список магазинов не найден", content = @Content)
@ApiResponse(responseCode = "500", description = "Непредвиденная ошибка сервера", content = @Content)
@Operation
public @interface SwaggerAnnotationForShopCollection {


    @AliasFor(annotation = Operation.class, attribute = "summary")
    String operation();
}
