package br.com.ecorank.ecorank.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Libera o acesso do front-end (outra origem/porta) para a API, mas somente
// para as origens autorizadas.
//
// Em produção, defina a variável de ambiente CORS_ORIGINS com o(s) endereço(s)
// do front-end separados por vírgula. Exemplo:
//   CORS_ORIGINS=https://meu-front.vercel.app,https://www.meusite.com.br
// Com a variável definida (produção), SOMENTE esses endereços são aceitos.
// Sem ela (desenvolvimento), só os endereços locais abaixo são aceitos.
@Component
public class CorsFilter implements Filter {

    private static final List<String> ORIGENS_LOCAIS = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:4173",
            "http://localhost:3000"
    );

    private final List<String> origensPermitidas = carregarOrigens();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String origem = req.getHeader("Origin");
        boolean origemPermitida = origem != null && origensPermitidas.contains(origem);

        // a resposta muda de acordo com a origem, então os caches precisam saber
        res.addHeader("Vary", "Origin");

        if (origemPermitida) {
            res.setHeader("Access-Control-Allow-Origin", origem);
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            res.setHeader("Access-Control-Max-Age", "3600");
        }

        // O navegador envia um OPTIONS antes do POST/PUT (preflight)
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(origemPermitida
                    ? HttpServletResponse.SC_OK
                    : HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    private static List<String> carregarOrigens() {
        List<String> origens = new ArrayList<>();
        String configuradas = System.getenv("CORS_ORIGINS");

        if (configuradas != null) {
            for (String origem : configuradas.split(",")) {
                String limpa = origem.trim();

                // remove a barra final (o navegador envia a origem sem ela)
                while (limpa.endsWith("/")) {
                    limpa = limpa.substring(0, limpa.length() - 1);
                }

                if (!limpa.isEmpty() && !limpa.equals("*")) {
                    origens.add(limpa);
                }
            }
        }
        return origens.isEmpty() ? ORIGENS_LOCAIS : origens;
    }
}
