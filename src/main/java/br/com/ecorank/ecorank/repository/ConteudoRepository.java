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
public class ConteudoRepository {

    private final DataSource dataSource;

    public ConteudoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Conteudo> findAll() {
        String sql = "SELECT ID, CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL FROM CONTEUDO ORDER BY ID";
        List<Conteudo> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resp.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando os conteúdos", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    private Conteudo montar(ResultSet rs) throws SQLException {
        Conteudo x = new Conteudo();
        x.setId(rs.getLong("ID"));
        x.setCategoria(rs.getString("CATEGORIA"));
        x.setTitulo(rs.getString("TITULO"));
        x.setResumo(rs.getString("RESUMO"));
        x.setFonte(rs.getString("FONTE"));
        x.setData(rs.getDate("DATA_PUBLICACAO").toLocalDate());
        x.setUrl(rs.getString("URL"));
        return x;
    }

    public void save(Conteudo x) {
        String sql = "INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setString(1, x.getCategoria());
            pstmt.setString(2, x.getTitulo());
            pstmt.setString(3, x.getResumo());
            pstmt.setString(4, x.getFonte());
            pstmt.setDate(5, Date.valueOf(x.getData()));
            pstmt.setString(6, x.getUrl());

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
            throw new RuntimeException("Erro ao inserir o conteúdo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public Conteudo findById(long id) {
        String sql = "SELECT ID, CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL FROM CONTEUDO WHERE ID = ?";
        Conteudo resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resp = montar(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando o conteúdo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public void update(Conteudo x) {
        String sql = "UPDATE CONTEUDO SET CATEGORIA = ?, TITULO = ?, RESUMO = ?, FONTE = ?, DATA_PUBLICACAO = ?, URL = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, x.getCategoria());
            pstmt.setString(2, x.getTitulo());
            pstmt.setString(3, x.getResumo());
            pstmt.setString(4, x.getFonte());
            pstmt.setDate(5, Date.valueOf(x.getData()));
            pstmt.setString(6, x.getUrl());
            pstmt.setLong(7, x.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o conteúdo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM CONTEUDO WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir o conteúdo", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}
