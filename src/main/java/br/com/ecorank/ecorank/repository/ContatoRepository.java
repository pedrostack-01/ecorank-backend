package br.com.ecorank.ecorank.repository;

import br.com.ecorank.ecorank.model.*;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ContatoRepository {

    private final DataSource dataSource;

    public ContatoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Contato x) {
        String sql = "INSERT INTO CONTATO (NOME, EMAIL, MOTIVO, MENSAGEM, STATUS) VALUES (?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setString(1, x.getNome());
            pstmt.setString(2, x.getEmail());
            pstmt.setString(3, x.getMotivo());
            pstmt.setString(4, x.getMensagem());
            pstmt.setString(5, x.getStatus());

            int registrosAfetados = pstmt.executeUpdate();

            if (registrosAfetados == 0) {
                throw new RuntimeException("Nenhum registro inserido no banco");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    x.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir a mensagem de contato", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    // Quantas mensagens chegaram na última hora (de um e-mail ou de todos)
    public int contarUltimaHora(String email) {
        String sql = "SELECT COUNT(*) FROM CONTATO WHERE DATA_ENVIO > SYSDATE - 1/24";
        if (email != null) {
            sql += " AND LOWER(EMAIL) = LOWER(?)";
        }
        int total = 0;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            if (email != null) {
                pstmt.setString(1, email);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro contando as mensagens de contato", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return total;
    }

    public List<Contato> findAll() {
        String sql = "SELECT ID, NOME, EMAIL, MOTIVO, MENSAGEM, STATUS FROM CONTATO ORDER BY ID DESC";
        List<Contato> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                resp.add(montar(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando as mensagens de contato", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public Contato findById(long id) {
        String sql = "SELECT ID, NOME, EMAIL, MOTIVO, MENSAGEM, STATUS FROM CONTATO WHERE ID = ?";
        Contato resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resp = montar(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a mensagem de contato", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public void updateStatus(long id, String status) {
        String sql = "UPDATE CONTATO SET STATUS = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setLong(2, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a mensagem de contato", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM CONTATO WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir a mensagem de contato", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Contato montar(ResultSet rs) throws SQLException {
        Contato x = new Contato();
        x.setId(rs.getLong("ID"));
        x.setNome(rs.getString("NOME"));
        x.setEmail(rs.getString("EMAIL"));
        x.setMotivo(rs.getString("MOTIVO"));
        x.setMensagem(rs.getString("MENSAGEM"));
        x.setStatus(rs.getString("STATUS"));
        return x;
    }
}
