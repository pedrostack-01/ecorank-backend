package br.com.ecorank.ecorank.repository;

import br.com.ecorank.ecorank.model.Conquista;
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
public class ConquistaRepository {

    private static final String COLUNAS =
            "ID, ID_USUARIO, NOME, DESCRICAO, DATA_CONQUISTA";

    private final DataSource dataSource;

    public ConquistaRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Conquista conquista) {
        String sql = "INSERT INTO CONQUISTA (ID_USUARIO, NOME, DESCRICAO, DATA_CONQUISTA) "
                + "VALUES (?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setLong(1, conquista.getIdUsuario());
            pstmt.setString(2, conquista.getNome());
            pstmt.setString(3, conquista.getDescricao());
            pstmt.setDate(4, Date.valueOf(conquista.getDataConquista()));

            int registrosAfetados = pstmt.executeUpdate();

            if (registrosAfetados == 0) {
                throw new RuntimeException("Nenhum registro inserido no banco");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    conquista.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir a conquista", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public List<Conquista> findAll() {
        String sql = "SELECT " + COLUNAS + " FROM CONQUISTA ORDER BY ID";
        List<Conquista> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                resp.add(montarConquista(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando as conquistas", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public List<Conquista> findByUsuario(long idUsuario) {
        String sql = "SELECT " + COLUNAS + " FROM CONQUISTA "
                + "WHERE ID_USUARIO = ? ORDER BY ID";
        List<Conquista> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resp.add(montarConquista(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando as conquistas do usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public Conquista findById(long id) {
        String sql = "SELECT " + COLUNAS + " FROM CONQUISTA WHERE ID = ?";
        Conquista conquista = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    conquista = montarConquista(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a conquista", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return conquista;
    }

    public void update(Conquista conquista) {
        String sql = "UPDATE CONQUISTA SET NOME = ?, DESCRICAO = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, conquista.getNome());
            pstmt.setString(2, conquista.getDescricao());
            pstmt.setLong(3, conquista.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a conquista", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM CONQUISTA WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir a conquista", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private Conquista montarConquista(ResultSet rs) throws SQLException {
        Conquista conquista = new Conquista();
        conquista.setId(rs.getLong("ID"));
        conquista.setIdUsuario(rs.getLong("ID_USUARIO"));
        conquista.setNome(rs.getString("NOME"));
        conquista.setDescricao(rs.getString("DESCRICAO"));

        Date data = rs.getDate("DATA_CONQUISTA");
        if (data != null) {
            conquista.setDataConquista(data.toLocalDate());
        }
        return conquista;
    }
}
