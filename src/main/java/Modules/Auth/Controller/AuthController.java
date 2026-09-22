
package Modules.Auth.Controller;


import Modules.Auth.Dto.Request.LoginRequestDto;
import Modules.Auth.Dto.Response.LoginResponse;
import Modules.Auth.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth" )
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        String token = authService.login(
                request.getCpf(),
                request.getSenha()
        );

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }
}
