package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Aluno;
import com.mycompany.myapp.domain.enumeration.Status;
import com.mycompany.myapp.repository.AlunoRepository;
import com.mycompany.myapp.service.AlunoService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

/**
 * Integration tests for the {@link AlunoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AlunoResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DATA_NASCIMENTO = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATA_NASCIMENTO = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_RM = 1;
    private static final Integer UPDATED_RM = 2;

    private static final byte[] DEFAULT_FOTO = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_FOTO = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_FOTO_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_FOTO_CONTENT_TYPE = "image/png";

    private static final Status DEFAULT_STATUS = Status.PREATIVO;
    private static final Status UPDATED_STATUS = Status.ATIVO;

    private static final String ENTITY_API_URL = "/api/alunos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AlunoRepository alunoRepository;

    @Mock
    private AlunoRepository alunoRepositoryMock;

    @Mock
    private AlunoService alunoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlunoMockMvc;

    private Aluno aluno;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Aluno createEntity(EntityManager em) {
        Aluno aluno = new Aluno()
            .nome(DEFAULT_NOME)
            .email(DEFAULT_EMAIL)
            .dataNascimento(DEFAULT_DATA_NASCIMENTO)
            .rm(DEFAULT_RM)
            .foto(DEFAULT_FOTO)
            .fotoContentType(DEFAULT_FOTO_CONTENT_TYPE)
            .status(DEFAULT_STATUS);
        return aluno;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Aluno createUpdatedEntity(EntityManager em) {
        Aluno aluno = new Aluno()
            .nome(UPDATED_NOME)
            .email(UPDATED_EMAIL)
            .dataNascimento(UPDATED_DATA_NASCIMENTO)
            .rm(UPDATED_RM)
            .foto(UPDATED_FOTO)
            .fotoContentType(UPDATED_FOTO_CONTENT_TYPE)
            .status(UPDATED_STATUS);
        return aluno;
    }

    @BeforeEach
    public void initTest() {
        aluno = createEntity(em);
    }

    @Test
    @Transactional
    void createAluno() throws Exception {
        int databaseSizeBeforeCreate = alunoRepository.findAll().size();
        // Create the Aluno
        restAlunoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aluno))
            )
            .andExpect(status().isCreated());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeCreate + 1);
        Aluno testAluno = alunoList.get(alunoList.size() - 1);
        assertThat(testAluno.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testAluno.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testAluno.getDataNascimento()).isEqualTo(DEFAULT_DATA_NASCIMENTO);
        assertThat(testAluno.getRm()).isEqualTo(DEFAULT_RM);
        assertThat(testAluno.getFoto()).isEqualTo(DEFAULT_FOTO);
        assertThat(testAluno.getFotoContentType()).isEqualTo(DEFAULT_FOTO_CONTENT_TYPE);
        assertThat(testAluno.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void createAlunoWithExistingId() throws Exception {
        // Create the Aluno with an existing ID
        aluno.setId(1L);

        int databaseSizeBeforeCreate = alunoRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlunoMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aluno))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllAlunos() throws Exception {
        // Initialize the database
        alunoRepository.saveAndFlush(aluno);

        // Get all the alunoList
        restAlunoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(aluno.getId().intValue())))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].dataNascimento").value(hasItem(DEFAULT_DATA_NASCIMENTO.toString())))
            .andExpect(jsonPath("$.[*].rm").value(hasItem(DEFAULT_RM)))
            .andExpect(jsonPath("$.[*].fotoContentType").value(hasItem(DEFAULT_FOTO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].foto").value(hasItem(Base64Utils.encodeToString(DEFAULT_FOTO))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlunosWithEagerRelationshipsIsEnabled() throws Exception {
        when(alunoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlunoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(alunoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlunosWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(alunoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlunoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(alunoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getAluno() throws Exception {
        // Initialize the database
        alunoRepository.saveAndFlush(aluno);

        // Get the aluno
        restAlunoMockMvc
            .perform(get(ENTITY_API_URL_ID, aluno.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(aluno.getId().intValue()))
            .andExpect(jsonPath("$.nome").value(DEFAULT_NOME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.dataNascimento").value(DEFAULT_DATA_NASCIMENTO.toString()))
            .andExpect(jsonPath("$.rm").value(DEFAULT_RM))
            .andExpect(jsonPath("$.fotoContentType").value(DEFAULT_FOTO_CONTENT_TYPE))
            .andExpect(jsonPath("$.foto").value(Base64Utils.encodeToString(DEFAULT_FOTO)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingAluno() throws Exception {
        // Get the aluno
        restAlunoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewAluno() throws Exception {
        // Initialize the database
        alunoRepository.saveAndFlush(aluno);

        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();

        // Update the aluno
        Aluno updatedAluno = alunoRepository.findById(aluno.getId()).get();
        // Disconnect from session so that the updates on updatedAluno are not directly saved in db
        em.detach(updatedAluno);
        updatedAluno
            .nome(UPDATED_NOME)
            .email(UPDATED_EMAIL)
            .dataNascimento(UPDATED_DATA_NASCIMENTO)
            .rm(UPDATED_RM)
            .foto(UPDATED_FOTO)
            .fotoContentType(UPDATED_FOTO_CONTENT_TYPE)
            .status(UPDATED_STATUS);

        restAlunoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAluno.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAluno))
            )
            .andExpect(status().isOk());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
        Aluno testAluno = alunoList.get(alunoList.size() - 1);
        assertThat(testAluno.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testAluno.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testAluno.getDataNascimento()).isEqualTo(UPDATED_DATA_NASCIMENTO);
        assertThat(testAluno.getRm()).isEqualTo(UPDATED_RM);
        assertThat(testAluno.getFoto()).isEqualTo(UPDATED_FOTO);
        assertThat(testAluno.getFotoContentType()).isEqualTo(UPDATED_FOTO_CONTENT_TYPE);
        assertThat(testAluno.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void putNonExistingAluno() throws Exception {
        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();
        aluno.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlunoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, aluno.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(aluno))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAluno() throws Exception {
        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();
        aluno.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlunoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(aluno))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAluno() throws Exception {
        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();
        aluno.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlunoMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aluno))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAlunoWithPatch() throws Exception {
        // Initialize the database
        alunoRepository.saveAndFlush(aluno);

        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();

        // Update the aluno using partial update
        Aluno partialUpdatedAluno = new Aluno();
        partialUpdatedAluno.setId(aluno.getId());

        partialUpdatedAluno
            .nome(UPDATED_NOME)
            .email(UPDATED_EMAIL)
            .rm(UPDATED_RM)
            .foto(UPDATED_FOTO)
            .fotoContentType(UPDATED_FOTO_CONTENT_TYPE)
            .status(UPDATED_STATUS);

        restAlunoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAluno.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAluno))
            )
            .andExpect(status().isOk());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
        Aluno testAluno = alunoList.get(alunoList.size() - 1);
        assertThat(testAluno.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testAluno.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testAluno.getDataNascimento()).isEqualTo(DEFAULT_DATA_NASCIMENTO);
        assertThat(testAluno.getRm()).isEqualTo(UPDATED_RM);
        assertThat(testAluno.getFoto()).isEqualTo(UPDATED_FOTO);
        assertThat(testAluno.getFotoContentType()).isEqualTo(UPDATED_FOTO_CONTENT_TYPE);
        assertThat(testAluno.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void fullUpdateAlunoWithPatch() throws Exception {
        // Initialize the database
        alunoRepository.saveAndFlush(aluno);

        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();

        // Update the aluno using partial update
        Aluno partialUpdatedAluno = new Aluno();
        partialUpdatedAluno.setId(aluno.getId());

        partialUpdatedAluno
            .nome(UPDATED_NOME)
            .email(UPDATED_EMAIL)
            .dataNascimento(UPDATED_DATA_NASCIMENTO)
            .rm(UPDATED_RM)
            .foto(UPDATED_FOTO)
            .fotoContentType(UPDATED_FOTO_CONTENT_TYPE)
            .status(UPDATED_STATUS);

        restAlunoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAluno.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAluno))
            )
            .andExpect(status().isOk());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
        Aluno testAluno = alunoList.get(alunoList.size() - 1);
        assertThat(testAluno.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testAluno.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testAluno.getDataNascimento()).isEqualTo(UPDATED_DATA_NASCIMENTO);
        assertThat(testAluno.getRm()).isEqualTo(UPDATED_RM);
        assertThat(testAluno.getFoto()).isEqualTo(UPDATED_FOTO);
        assertThat(testAluno.getFotoContentType()).isEqualTo(UPDATED_FOTO_CONTENT_TYPE);
        assertThat(testAluno.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void patchNonExistingAluno() throws Exception {
        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();
        aluno.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlunoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, aluno.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(aluno))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAluno() throws Exception {
        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();
        aluno.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlunoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(aluno))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAluno() throws Exception {
        int databaseSizeBeforeUpdate = alunoRepository.findAll().size();
        aluno.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlunoMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(aluno))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Aluno in the database
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAluno() throws Exception {
        // Initialize the database
        alunoRepository.saveAndFlush(aluno);

        int databaseSizeBeforeDelete = alunoRepository.findAll().size();

        // Delete the aluno
        restAlunoMockMvc
            .perform(delete(ENTITY_API_URL_ID, aluno.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Aluno> alunoList = alunoRepository.findAll();
        assertThat(alunoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
