package gr.aueb.cf.eduapp.validator;

import gr.aueb.cf.eduapp.dto.UserInsertDTO;
import gr.aueb.cf.eduapp.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInsertValidatorTest {

    @Mock
    private IUserService userService;

    private UserInsertValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UserInsertValidator(userService);
    }

    @Test
    void supportsOnlyUserInsertDTO() {
        assertTrue(validator.supports(UserInsertDTO.class));
        assertFalse(validator.supports(String.class));
    }

    @Test
    void validateRejectsWhenUsernameAlreadyExists() {
        UserInsertDTO dto = new UserInsertDTO("alice", "Secret1!", 1L);
        when(userService.isUserExists("alice")).thenReturn(true);

        Errors errors = new MapBindingResult(new HashMap<>(), "userInsertDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("username"));
        assertEquals("username.user.exists", errors.getFieldError("username").getCode());
    }

    @Test
    void validatePassesWhenUsernameIsAvailable() {
        UserInsertDTO dto = new UserInsertDTO("newuser", "Secret1!", 1L);
        when(userService.isUserExists("newuser")).thenReturn(false);

        Errors errors = new MapBindingResult(new HashMap<>(), "userInsertDTO");
        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }
}
