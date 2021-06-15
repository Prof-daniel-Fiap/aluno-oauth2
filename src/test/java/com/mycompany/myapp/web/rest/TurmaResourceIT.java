package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Turma;
import com.mycompany.myapp.repository.TurmaRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

/**
 * Integration tests for the {@link TurmaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TurmaResourceIT {

    private static final String DEFAULT_CODIGO_TURMA = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO_TURMA = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DATA_CRIACAO = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATA_CRIACAO = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_UNIDADE = "AAAAAAAAAA";
    private static final String UPDATED_UNIDADE = "BBBBBBBBBB";

    private static final String DEFAULT_OBSERVACOES = "AAAAAAAAAA";
    private static final String UPDATED_OBSERVACOES = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/turmas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTurmaMockMvc;

    private Turma turma;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Turma createEntity(EntityManager em) {
        Turma turma = new Turma()
            .codigoTurma(DEFAULT_CODIGO_TURMA)
            .dataCriacao(DEFAULT_DATA_CRIACAO)
            .unidade(DEFAULT_UNIDADE)
            .observacoes(DEFAULT_OBSERVACOES);
        return turma;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Turma createUpdatedEntity(EntityManager em) {
        Turma turma = new Turma()
            .codigoTurma(UPDATED_CODIGO_TURMA)
            .dataCriacao(UPDATED_DATA_CRIACAO)
            .unidade(UPDATED_UNIDADE)
            .observacoes(UPDATED_OBSERVACOES);
        return turma;
    }

    @BeforeEach
    public void initTest() {
        turma = createEntity(em);
    }

    @Test
    @Transactional
    void createTurma() throws Exception {
        int databaseSizeBeforeCreate = turmaRepository.findAll().size();
        // Create the Turma
        restTurmaMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(turma))
            )
            .andExpect(status().isCreated());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeCreate + 1);
        Turma testTurma = turmaList.get(turmaList.size() - 1);
        assertThat(testTurma.getCodigoTurma()).isEqualTo(DEFAULT_CODIGO_TURMA);
        assertThat(testTurma.getDataCriacao()).isEqualTo(DEFAULT_DATA_CRIACAO);
        assertThat(testTurma.getUnidade()).isEqualTo(DEFAULT_UNIDADE);
        assertThat(testTurma.getObservacoes()).isEqualTo(DEFAULT_OBSERVACOES);
    }

    @Test
    @Transactional
    void createTurmaWithExistingId() throws Exception {
        // Create the Turma with an existing ID
        turma.setId(1L);

        int databaseSizeBeforeCreate = turmaRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTurmaMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(turma))
            )
            .andExpect(status().isBadRequest());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllTurmas() throws Exception {
        // Initialize the database
        turmaRepository.saveAndFlush(turma);

        // Get all the turmaList
        restTurmaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(turma.getId().intValue())))
            .andExpect(jsonPath("$.[*].codigoTurma").value(hasItem(DEFAULT_CODIGO_TURMA)))
            .andExpect(jsonPath("$.[*].dataCriacao").value(hasItem(DEFAULT_DATA_CRIACAO.toString())))
            .andExpect(jsonPath("$.[*].unidade").value(hasItem(DEFAULT_UNIDADE)))
            .andExpect(jsonPath("$.[*].observacoes").value(hasItem(DEFAULT_OBSERVACOES.toString())));
    }

    @Test
    @Transactional
    void getTurma() throws Exception {
        // Initialize the database
        turmaRepository.saveAndFlush(turma);

        // Get the turma
        restTurmaMockMvc
            .perform(get(ENTITY_API_URL_ID, turma.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(turma.getId().intValue()))
            .andExpect(jsonPath("$.codigoTurma").value(DEFAULT_CODIGO_TURMA))
            .andExpect(jsonPath("$.dataCriacao").value(DEFAULT_DATA_CRIACAO.toString()))
            .andExpect(jsonPath("$.unidade").value(DEFAULT_UNIDADE))
            .andExpect(jsonPath("$.observacoes").value(DEFAULT_OBSERVACOES.toString()));
    }

    @Test
    @Transactional
    void getNonExistingTurma() throws Exception {
        // Get the turma
        restTurmaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewTurma() throws Exception {
        // Initialize the database
        turmaRepository.saveAndFlush(turma);

        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();

        // Update the turma
        Turma updatedTurma = turmaRepository.findById(turma.getId()).get();
        // Disconnect from session so that the updates on updatedTurma are not directly saved in db
        em.detach(updatedTurma);
        updatedTurma
            .codigoTurma(UPDATED_CODIGO_TURMA)
            .dataCriacao(UPDATED_DATA_CRIACAO)
            .unidade(UPDATED_UNIDADE)
            .observacoes(UPDATED_OBSERVACOES);

        restTurmaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTurma.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTurma))
            )
            .andExpect(status().isOk());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
        Turma testTurma = turmaList.get(turmaList.size() - 1);
        assertThat(testTurma.getCodigoTurma()).isEqualTo(UPDATED_CODIGO_TURMA);
        assertThat(testTurma.getDataCriacao()).isEqualTo(UPDATED_DATA_CRIACAO);
        assertThat(testTurma.getUnidade()).isEqualTo(UPDATED_UNIDADE);
        assertThat(testTurma.getObservacoes()).isEqualTo(UPDATED_OBSERVACOES);
    }

    @Test
    @Transactional
    void putNonExistingTurma() throws Exception {
        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();
        turma.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTurmaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, turma.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(turma))
            )
            .andExpect(status().isBadRequest());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTurma() throws Exception {
        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();
        turma.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTurmaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(turma))
            )
            .andExpect(status().isBadRequest());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTurma() throws Exception {
        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();
        turma.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTurmaMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(turma))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTurmaWithPatch() throws Exception {
        // Initialize the database
        turmaRepository.saveAndFlush(turma);

        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();

        // Update the turma using partial update
        Turma partialUpdatedTurma = new Turma();
        partialUpdatedTurma.setId(turma.getId());

        partialUpdatedTurma.dataCriacao(UPDATED_DATA_CRIACAO).observacoes(UPDATED_OBSERVACOES);

        restTurmaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTurma.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTurma))
            )
            .andExpect(status().isOk());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
        Turma testTurma = turmaList.get(turmaList.size() - 1);
        assertThat(testTurma.getCodigoTurma()).isEqualTo(DEFAULT_CODIGO_TURMA);
        assertThat(testTurma.getDataCriacao()).isEqualTo(UPDATED_DATA_CRIACAO);
        assertThat(testTurma.getUnidade()).isEqualTo(DEFAULT_UNIDADE);
        assertThat(testTurma.getObservacoes()).isEqualTo(UPDATED_OBSERVACOES);
    }

    @Test
    @Transactional
    void fullUpdateTurmaWithPatch() throws Exception {
        // Initialize the database
        turmaRepository.saveAndFlush(turma);

        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();

        // Update the turma using partial update
        Turma partialUpdatedTurma = new Turma();
        partialUpdatedTurma.setId(turma.getId());

        partialUpdatedTurma
            .codigoTurma(UPDATED_CODIGO_TURMA)
            .dataCriacao(UPDATED_DATA_CRIACAO)
            .unidade(UPDATED_UNIDADE)
            .observacoes(UPDATED_OBSERVACOES);

        restTurmaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTurma.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTurma))
            )
            .andExpect(status().isOk());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
        Turma testTurma = turmaList.get(turmaList.size() - 1);
        assertThat(testTurma.getCodigoTurma()).isEqualTo(UPDATED_CODIGO_TURMA);
        assertThat(testTurma.getDataCriacao()).isEqualTo(UPDATED_DATA_CRIACAO);
        assertThat(testTurma.getUnidade()).isEqualTo(UPDATED_UNIDADE);
        assertThat(testTurma.getObservacoes()).isEqualTo(UPDATED_OBSERVACOES);
    }

    @Test
    @Transactional
    void patchNonExistingTurma() throws Exception {
        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();
        turma.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTurmaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, turma.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(turma))
            )
            .andExpect(status().isBadRequest());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTurma() throws Exception {
        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();
        turma.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTurmaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(turma))
            )
            .andExpect(status().isBadRequest());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTurma() throws Exception {
        int databaseSizeBeforeUpdate = turmaRepository.findAll().size();
        turma.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTurmaMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(turma))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Turma in the database
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTurma() throws Exception {
        // Initialize the database
        turmaRepository.saveAndFlush(turma);

        int databaseSizeBeforeDelete = turmaRepository.findAll().size();

        // Delete the turma
        restTurmaMockMvc
            .perform(delete(ENTITY_API_URL_ID, turma.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Turma> turmaList = turmaRepository.findAll();
        assertThat(turmaList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
