package mj.carthy.easyutils.validator

import mj.carthy.easyutils.annotation.Password
import org.apache.commons.lang3.StringUtils
import org.passay.*
import org.passay.dictionary.WordListDictionary
import org.passay.dictionary.WordLists.createFromReader
import org.passay.dictionary.sort.ArraysSort
import java.io.FileReader
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.util.Collections.singleton
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PasswordConstraintValidator: ConstraintValidator<Password, String> {

    private var dictionaryRule: DictionaryRule? = null;

    companion object {
        private const val DELIMITER = ","
        private const val PATH_TO_INVALID_PASSWORD_FILE = "/invalid-password-list.txt"
        private const val NUMBER_OF_ELEMENTS = 1
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_LENGTH = 30
    }

    override fun initialize(
            constraintAnnotation: Password
    ) {
      FileReader(this.javaClass.getResource(PATH_TO_INVALID_PASSWORD_FILE).file).use {
        dictionaryRule = DictionaryRule(WordListDictionary(createFromReader(singleton(it).toTypedArray(), FALSE, ArraysSort())))
      }
    }

    override fun isValid(password: String, context: ConstraintValidatorContext): Boolean {
        val validator = PasswordValidator(mutableListOf(
                // at least 8 characters
                LengthRule(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH),
                // at least one upper-case character
                CharacterRule(EnglishCharacterData.UpperCase, NUMBER_OF_ELEMENTS),
                // at least one lower-case character
                CharacterRule(EnglishCharacterData.LowerCase, NUMBER_OF_ELEMENTS),
                // at least one digit character
                CharacterRule(EnglishCharacterData.Digit, NUMBER_OF_ELEMENTS),
                // at least one symbol (special character)
                CharacterRule(EnglishCharacterData.Special, NUMBER_OF_ELEMENTS),
                // no whitespace
                WhitespaceRule(),
                // no common passwords
                dictionaryRule
        ))

        val result: RuleResult = validator.validate(PasswordData(password))

        if (result.isValid) return TRUE

        val messages: List<String> = validator.getMessages(result)
        val messageTemplate: String = StringUtils.joinWith(DELIMITER, messages)

        context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation().disableDefaultConstraintViolation()

        return FALSE
    }
}