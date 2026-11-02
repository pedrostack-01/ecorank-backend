package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.MuitasTentativasException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// Proteção contra tentativas excessivas de login: depois de 5 senhas erradas
// para o mesmo e-mail, novas tentativas ficam bloqueadas por 15 minutos.
// Os contadores ficam na memória (zeram se a aplicação reiniciar).
@Component
public class LimitadorLogin {

    private static final int MAX_FALHAS = 5;
    private static final long JANELA_MS = 15 * 60 * 1000L;

    // e-mail -> {quantidade de falhas, horário da última falha}
    private final Map<String, long[]> falhas = new HashMap<>();

    public synchronized void verificar(String email) {
        long[] registro = falhas.get(chave(email));
        if (registro == null) {
            return;
        }

        long agora = System.currentTimeMillis();

        if (agora - registro[1] > JANELA_MS) {
            falhas.remove(chave(email));
            return;
        }

        if (registro[0] >= MAX_FALHAS) {
            throw new MuitasTentativasException(
                    "Muitas tentativas de login. Tente novamente em alguns minutos.");
        }
    }

    public synchronized void registrarFalha(String email) {
        long agora = System.currentTimeMillis();
        long[] registro = falhas.get(chave(email));

        if (registro == null || agora - registro[1] > JANELA_MS) {
            registro = new long[]{0, agora};
        }

        registro[0]++;
        registro[1] = agora;
        falhas.put(chave(email), registro);

        limparAntigos(agora);
    }

    public synchronized void registrarSucesso(String email) {
        falhas.remove(chave(email));
    }

    private String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    // evita que o mapa cresça sem limite
    private void limparAntigos(long agora) {
        if (falhas.size() < 1000) {
            return;
        }
        falhas.values().removeIf(registro -> agora - registro[1] > JANELA_MS);
    }
}
