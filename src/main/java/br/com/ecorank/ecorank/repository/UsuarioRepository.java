package br.com.ecorank.ecorank.repository;

import br.com.ecorank.ecorank.model.Usuario;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioRepository {

    private final DataSource dataSource;

    public UsuarioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Usuario usuario) {
        String sql = "INSERT INTO USUARIO (NOME, EMAIL, PONTOS, NIVEL, RECOMPENSA) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"})) {

            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setInt(3, usuario.getPontos());
            pstmt.setString(4, usuario.getNivel());
            pstmt.setString(5, usuario.getRecompensa());

            int registrosAfetados = pstmt.executeUpdate();

            if (registrosAfetados == 0) {
                throw new RuntimeException("Nenhum registro inserido no banco");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir o usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public List<Usuario> findAll() {
        String sql = "SELECT ID, NOME, EMAIL, PONTOS, NIVEL, RECOMPENSA, PERFIL "
                + "FROM USUARIO ORDER BY ID";
        List<Usuario> resp = new ArrayList<>();
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                resp.add(montarUsuario(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando os usuários", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    public Usuario findById(long id) {
        String sql = "SELECT ID, NOME, EMAIL, PONTOS, NIVEL, RECOMPENSA, PERFIL "
                + "FROM USUARIO WHERE ID = ?";
        Usuario usuario = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = montarUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando o usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return usuario;
    }

    public Usuario findByEmail(String email) {
        String sql = "SELECT ID, NOME, EMAIL, PONTOS, NIVEL, RECOMPENSA, PERFIL "
                + "FROM USUARIO WHERE LOWER(EMAIL) = LOWER(?)";
        Usuario usuario = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = montarUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando o usuário pelo email", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return usuario;
    }

    public void update(Usuario usuario) {
        String sql = "UPDATE USUARIO SET NOME = ?, EMAIL = ?, PONTOS = ?, "
                + "NIVEL = ?, RECOMPENSA = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setInt(3, usuario.getPontos());
            pstmt.setString(4, usuario.getNivel());
            pstmt.setString(5, usuario.getRecompensa());
            pstmt.setLong(6, usuario.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM USUARIO WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir o usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    // Guarda o hash e o salt da senha (a senha nunca é salva em texto puro)
    public void salvarSenha(long id, String hash, String salt) {
        String sql = "UPDATE USUARIO SET SENHA_HASH = ?, SALT = ? WHERE ID = ?";
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, hash);
            pstmt.setString(2, salt);
            pstmt.setLong(3, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar a senha do usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    // Retorna {hash, salt} ou null se o usuário não tem senha cadastrada
    public String[] buscarSenha(long id) {
        String sql = "SELECT SENHA_HASH, SALT FROM USUARIO WHERE ID = ?";
        String[] resp = null;
        Connection con = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getString("SENHA_HASH") != null) {
                    resp = new String[]{rs.getString("SENHA_HASH"), rs.getString("SALT")};
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando a senha do usuário", e);
        } finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return resp;
    }

    private Usuario montarUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("ID"));
        usuario.setNome(rs.getString("NOME"));
        usuario.setEmail(rs.getString("EMAIL"));
        usuario.setPontos(rs.getInt("PONTOS"));
        usuario.setNivel(rs.getString("NIVEL"));

        // No Oracle um texto vazio e gravado como NULL
        String recompensa = rs.getString("RECOMPENSA");
        if (recompensa == null) {
            recompensa = "";
        }
        usuario.setRecompensa(recompensa);
        usuario.setPerfil(rs.getString("PERFIL"));
        return usuario;
    }
}
