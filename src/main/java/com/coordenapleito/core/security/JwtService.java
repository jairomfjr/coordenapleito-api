package com.coordenapleito.core.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.infrastructure.util.CpfUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final UsuarioRepository usuarioRepository;
    public JwtService(JwtEncoder encoder, JwtDecoder decoder, UsuarioRepository usuarioRepository) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuario(String username) {
        Optional<Usuario> opt = usuarioRepository.findByCpf(username);
        if (opt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado");
        }
        return opt.get();
    }

    public String generateToken(Authentication authentication) {
        Optional<Usuario> optUsuario = usuarioRepository.findByCpfWithGruposAndPermissoes(authentication.getName());
        if (optUsuario.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado");
        }
        return generateToken(optUsuario.get());
    }

    /** Emite JWT enxuto; permissões são resolvidas no banco em {@link CoordenapleitoJwtGrantedAuthoritiesConverter}. */
    public String generateToken(Usuario usuario) {
        Instant now = Instant.now();
        long expiry = 36000L;

        List<String> gruposNomes = usuario.getGrupos() != null
                ? usuario.getGrupos().stream()
                        .map(Grupo::getNome)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                : List.of();
        String grupoLegado = gruposNomes.isEmpty() ? "" : String.join(" ", gruposNomes);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("esqueleto-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(CpfUtils.normalizar(usuario.getCpf()))
                .claim("grupos", gruposNomes)
                .claim("grupo", grupoLegado)
                .claim("nome", usuario.getCpf())
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public void validateToken(String token) throws ExpiredJwtException {
        try {
            decoder.decode(token);
        } catch (MalformedJwtException ex) {
            throw new MalformedJwtException("Invalid JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new UnsupportedJwtException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT claims string is empty.");
        } catch (JwtException e) {
            throw new RuntimeException(e);
        }
    }
}