package br.com.ecorank.ecorank.repository;

import br.com.ecorank.ecorank.model.AcaoSustentavel;
import br.com.ecorank.ecorank.model.Status;
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
public class AcaoSustentavelRepository {

    private static final String COLUNAS =
            "ID, ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG";

    private final DataSource dataSource;

    public AcaoSustentavelRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(AcaoSustentavel acao) {
        String sql = "INSERT INTO ACAO_SUSTENTAVEL "
                + "(ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setLong(1, acao.getIdUsuario());
            pstmt.setString(2, acao.getCategoria());
            pstmt.setString(3, acao.getDescricao());
            pstmt.setString(4, acao.getLinkEvidencia());
            pstmt.setString(5, acao.getStatus().name());
            pstmt.setInt(6, acao.getPontos());
            pstmt.setDate(7, Date.valueOf(acao.getData()));
            pstmt.setDouble(8, acao.getCo2Kg());

            int registrosAfetados = pstmt.executeUpdate();

            if (registrosAfetados == 0) {
                throw new RuntimeException("Nenhum registro inserido no banco");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    acao.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir a ação sustentável", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public List<AcaoSustentavel> findAll() {
        String sql = "SELECT " + COLUNAS + " FROM ACAO_SUSTENTAVEL ORDER BY ID";
        List<AcaoSustentavel> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                resp.add(montarAcao(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando as ações sustentáveis", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public List<AcaoSustentavel> findByUsuario(long idUsuario) {
        String sql = "SELECT " + COLUNAS + " FROM ACAO_SUSTENTAVEL "
                + "WHERE ID_USUARIO = ? ORDER BY ID";
        List<AcaoSustentavel> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resp.add(montarAcao(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando as ações do usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public AcaoSustentavel findById(long id) {
        String sql = "SELECT " + COLUNAS + " FROM ACAO_SUSTENTAVEL WHERE ID = ?";
        AcaoSustentavel acao = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    acao = montarAcao(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a ação sustentável", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return acao;
    }

    public void update(AcaoSustentavel acao) {
        String sql = "UPDATE ACAO_SUSTENTAVEL SET CATEGORIA = ?, DESCRICAO = ?, "
                + "LINK_EVIDENCIA = ?, STATUS = ?, PONTOS = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, acao.getCategoria());
            pstmt.setString(2, acao.getDescricao());
            pstmt.setString(3, acao.getLinkEvidencia());
            pstmt.setString(4, acao.getStatus().name());
            pstmt.setInt(5, acao.getPontos());
            pstmt.setLong(6, acao.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a ação sustentável", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM ACAO_SUSTENTAVEL WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir a ação sustentável", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    private AcaoSustentavel montarAcao(ResultSet rs) throws SQLException {
        AcaoSustentavel acao = new AcaoSustentavel();
        acao.setId(rs.getLong("ID"));
        acao.setIdUsuario(rs.getLong("ID_USUARIO"));
        acao.setCategoria(rs.getString("CATEGORIA"));
        acao.setDescricao(rs.getString("DESCRICAO"));
        acao.setLinkEvidencia(rs.getString("LINK_EVIDENCIA"));
        acao.setStatus(Status.valueOf(rs.getString("STATUS")));
        acao.setPontos(rs.getInt("PONTOS"));

        acao.setCo2Kg(rs.getDouble("CO2_KG"));

        Date data = rs.getDate("DATA_ACAO");
        if (data != null) {
            acao.setData(data.toLocalDate());
        }
        return acao;
    }
}
