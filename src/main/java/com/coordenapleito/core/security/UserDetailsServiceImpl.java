package com.coordenapleito.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.infrastructure.util.CpfUtils;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cpfNormalizado = CpfUtils.normalizar(username);
        return usuarioRepository.findByCpf(cpfNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário com o cpf : " + username + ", não encontrado."));
    }
}