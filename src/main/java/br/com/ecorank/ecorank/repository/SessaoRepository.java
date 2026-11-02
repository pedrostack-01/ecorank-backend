package br.com.ecorank.ecorank.repository;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class SessaoRepository {

    private final DataSource dataSource;

    public SessaoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(String token, long idUsuario) {
        String sql = "INSERT INTO SESSAO (TOKEN, ID_USUARIO) VALUES (?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, token);
            pstmt.setLong(2, idUsuario);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar a sessão", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    // Retorna o id do usuário dono do token (0 se não existir ou se expirou: 7 dias)
    public long findUsuarioId(String token) {
        String sql = "SELECT ID_USUARIO FROM SESSAO WHERE TOKEN = ? "
                + "AND CRIADA_EM > SYSTIMESTAMP - INTERVAL '7' DAY";
        long idUsuario = 0;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, token);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a sessão", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return idUsuario;
    }

    // Remove as sessões que passaram de 7 dias (a validade usada em findUsuarioId)
    public void deleteExpiradas() {
        String sql = "DELETE FROM SESSAO WHERE CRIADA_EM <= SYSTIMESTAMP - INTERVAL '7' DAY";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar as sessões expiradas", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteByToken(String token) {
        String sql = "DELETE FROM SESSAO WHERE TOKEN = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, token);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao encerrar a sessão", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}
