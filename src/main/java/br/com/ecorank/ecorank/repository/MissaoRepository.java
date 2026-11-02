package br.com.ecorank.ecorank.repository;

import br.com.ecorank.ecorank.model.Missao;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MissaoRepository {

    private static final String COLUNAS =
            "ID, ID_USUARIO, DESCRICAO, OBJETIVO, PROGRESSO, RECOMPENSA, PRAZO";

    private final DataSource dataSource;

    public MissaoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Missao missao) {
        String sql = "INSERT INTO MISSAO "
                + "(ID_USUARIO, DESCRICAO, OBJETIVO, PROGRESSO, RECOMPENSA, PRAZO) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setLong(1, missao.getIdUsuario());
            pstmt.setString(2, missao.getDescricao());
            pstmt.setInt(3, missao.getObjetivo());
            pstmt.setInt(4, missao.getProgresso());
            pstmt.setInt(5, missao.getRecompensa());
            pstmt.setDate(6, Date.valueOf(missao.getPrazo()));

            int registrosAfetados = pstmt.executeUpdate();

            if (registrosAfetados == 0) {
                throw new RuntimeException("Nenhum registro inserido no banco");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    missao.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir a missão", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public List<Missao> findAll() {
        String sql = "SELECT " + COLUNAS + " FROM MISSAO ORDER BY ID";
        List<Missao> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                resp.add(montarMissao(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando as missões", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public List<Missao> findByUsuario(long idUsuario) {
        String sql = "SELECT " + COLUNAS + " FROM MISSAO "
                + "WHERE ID_USUARIO = ? ORDER BY ID";
        List<Missao> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resp.add(montarMissao(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando as missões do usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public Missao findById(long id) {
        String sql = "SELECT " + COLUNAS + " FROM MISSAO WHERE ID = ?";
        Missao missao = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    missao = montarMissao(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a missão", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return missao;
    }

    public void update(Missao missao) {
        String sql = "UPDATE MISSAO SET DESCRICAO = ?, OBJETIVO = ?, PROGRESSO = ?, "
                + "RECOMPENSA = ?, PRAZO = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, missao.getDescricao());
            pstmt.setInt(2, missao.getObjetivo());
            pstmt.setInt(3, missao.getProgresso());
            pstmt.setInt(4, missao.getRecompensa());
            pstmt.setDate(5, Date.valueOf(missao.getPrazo()));
            pstmt.setLong(6, missao.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a missão", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM MISSAO WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir a missão", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Missao montarMissao(ResultSet rs) throws SQLException {
        Missao missao = new Missao();
        missao.setId(rs.getLong("ID"));
        missao.setIdUsuario(rs.getLong("ID_USUARIO"));
        missao.setDescricao(rs.getString("DESCRICAO"));
        missao.setObjetivo(rs.getInt("OBJETIVO"));
        missao.setProgresso(rs.getInt("PROGRESSO"));
        missao.setRecompensa(rs.getInt("RECOMPENSA"));

        Date prazo = rs.getDate("PRAZO");
        if (prazo != null) {
            missao.setPrazo(prazo.toLocalDate());
        }
        return missao;
    }
}
