package fsoft.franchise.exception;

import fsoft.franchise.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Product domain specific error codes.
 */
@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(404, "Product not found", HttpStatus.NOT_FOUND, "product.not_found"),
    CATEGORY_NOT_FOUND(404, "Category not found", HttpStatus.NOT_FOUND, "category.not_found"),
    CATEGORY_INACTIVE(400, "Category is inactive", HttpStatus.BAD_REQUEST, "category.inactive"),
    VARIANT_NOT_FOUND(404, "Variant not found", HttpStatus.NOT_FOUND, "variant.not_found"),
    IMAGE_NOT_FOUND(404, "Image not found", HttpStatus.NOT_FOUND, "image.not_found"),
    INGREDIENT_NOT_FOUND(404, "Ingredient not found", HttpStatus.NOT_FOUND, "ingredient.not_found"),
    PRODUCT_ALREADY_EXISTS(400, "Product with this name already exists", HttpStatus.BAD_REQUEST, "product.already_exists"),
    PRODUCT_INCOMPLETE(400, "Product must have description, category, images, and variants before being activated", HttpStatus.BAD_REQUEST, "product.incomplete");

    private final int code;
    private final String message;
    private final HttpStatus status;
    private final String errorKey;

    @Override
    public String getDomain() {
        return "PRODUCT";
    }
}
