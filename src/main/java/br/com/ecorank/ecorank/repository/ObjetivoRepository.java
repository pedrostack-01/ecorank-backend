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
public class ObjetivoRepository {

    private final DataSource dataSource;

    public ObjetivoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Objetivo x) {
        String sql = "INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setLong(1, x.getIdUsuario());
            pstmt.setString(2, x.getTitulo());
            pstmt.setString(3, x.getDescricao());
            pstmt.setInt(4, x.getMeta());
            pstmt.setInt(5, x.getProgresso());

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
            throw new RuntimeException("Erro ao inserir o objetivo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public List<Objetivo> findByUsuario(long idUsuario) {
        String sql = "SELECT ID, ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO FROM OBJETIVO WHERE ID_USUARIO = ? ORDER BY ID";
        List<Objetivo> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resp.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando os objetivos", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public Objetivo findById(long id) {
        String sql = "SELECT ID, ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO FROM OBJETIVO WHERE ID = ?";
        Objetivo resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resp = montar(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando o objetivo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public void update(Objetivo x) {
        String sql = "UPDATE OBJETIVO SET TITULO = ?, DESCRICAO = ?, META = ?, PROGRESSO = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, x.getTitulo());
            pstmt.setString(2, x.getDescricao());
            pstmt.setInt(3, x.getMeta());
            pstmt.setInt(4, x.getProgresso());
            pstmt.setLong(5, x.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o objetivo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM OBJETIVO WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir o objetivo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Objetivo montar(ResultSet rs) throws SQLException {
        Objetivo x = new Objetivo();
        x.setId(rs.getLong("ID"));
        x.setIdUsuario(rs.getLong("ID_USUARIO"));
        x.setTitulo(rs.getString("TITULO"));

        // No Oracle um texto vazio e gravado como NULL
        String descricao = rs.getString("DESCRICAO");
        x.setDescricao(descricao == null ? "" : descricao);

        x.setMeta(rs.getInt("META"));
        x.setProgresso(rs.getInt("PROGRESSO"));
        return x;
    }
}
