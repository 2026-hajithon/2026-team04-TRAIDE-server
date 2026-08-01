package com.gdghajithon.auth;

import com.gdghajithon.auth.dto.AuthResponse;
import com.gdghajithon.auth.dto.LoginRequest;
import com.gdghajithon.auth.dto.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "계정 회원가입 및 로그인 API")
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String SUCCESS_EXAMPLE = """
            {
              "userId": 1,
              "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
              "tokenType": "Bearer",
              "expiresInSeconds": 3600
            }
            """;

    private static final String VALIDATION_ERROR_EXAMPLE = """
            {
              "code": "VALIDATION_ERROR",
              "message": "입력값을 확인해주세요."
            }
            """;

    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "아이디와 비밀번호로 User 계정만 생성하고 JWT Access Token을 발급합니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = SignupRequest.class),
                    examples = @ExampleObject(value = "{\"loginId\":\"user01\",\"password\":\"password123\"}")
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE))),
            @ApiResponse(responseCode = "409", description = "중복 아이디",
                    content = @Content(examples = @ExampleObject(value = """
                            {"code":"LOGIN_ID_ALREADY_EXISTS","message":"이미 사용 중인 아이디입니다."}
                            """)))
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호를 검증하고 JWT Access Token을 발급합니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(value = "{\"loginId\":\"user01\",\"password\":\"password123\"}")
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 정보 불일치",
                    content = @Content(examples = @ExampleObject(value = """
                            {"code":"INVALID_CREDENTIALS","message":"아이디 또는 비밀번호가 올바르지 않습니다."}
                            """)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
