package us.mn.state.dot.mnroad.data

/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jun 8, 2011
 * Time: 10:02:16 AM
 */
class ExportMaterialSamplesJobParams {
  String rdrive
  String dataProductDataFolder
  String materialSamplesFolder
  String overwriteMatSamplesFiles
  String tableName
  String fileName
  String countQuery
  ArrayList<String> mat_samples_columns
  String selectPhrase
  String querySuffix
  String queryOrder
  String wherePhrase
  String testQueryOrder
  String asphaltFolder
  String concreteFolder
  String aggregateSoilFolder
  ArrayList<String> mat_binder_abcd_test
  ArrayList<String> mat_binder_bbr_test
  ArrayList<String> mat_binder_critical_crack_temp
  ArrayList<String> mat_binder_dent_fracture
  ArrayList<String> mat_binder_dilatometr_tst
  ArrayList<String> mat_binder_dsr_tests
  ArrayList<String> mat_binder_dt_test
  ArrayList<String> mat_binder_fatigue
  ArrayList<String> mat_binder_repeated_creep
  ArrayList<String> mat_binder_strain_sweeps
  ArrayList<String> mat_binder_trad_tests
  ArrayList<String> mat_core_lengths
  ArrayList<String> mat_hma_aging
  ArrayList<String> mat_hma_apa
  ArrayList<String> mat_hma_bbr_test
  ArrayList<String> mat_hma_complex_shear_modu
  ArrayList<String> mat_hma_core_tests
  ArrayList<String> mat_hma_dct_test
  ArrayList<String> mat_hma_dilatometric_test
  ArrayList<String> mat_hma_dynamic_modulus
  ArrayList<String> mat_hma_flow_number
  ArrayList<String> mat_hma_hamburg
  ArrayList<String> mat_hma_idt_test
  ArrayList<String> mat_hma_indirect_tens_fati
  ArrayList<String> mat_hma_mix_tests
  ArrayList<String> mat_hma_original_density_air
  ArrayList<String> mat_hma_repeat_perm_deform
  ArrayList<String> mat_hma_repeat_shear
  ArrayList<String> mat_hma_scb_test
  ArrayList<String> mat_hma_senb_test
  ArrayList<String> mat_hma_sieve_data
  ArrayList<String> mat_hma_triaxial_static_creep
  ArrayList<String> mat_hma_triaxial_strength
  ArrayList<String> mat_hma_tsrst_test
  ArrayList<String> mat_hma_tti_overlay
  ArrayList<String> mat_hma_ultrasonic_modulus
  ArrayList<String> mat_conc_air_void_results
  ArrayList<String> mat_conc_field_results
  ArrayList<String> mat_conc_flex_strength
  ArrayList<String> mat_conc_freeze_thaw_results
  ArrayList<String> mat_conc_mod_poisson_results
  ArrayList<String> mat_conc_rapid_cloride
  ArrayList<String> mat_conc_strength_results
  ArrayList<String> mat_conc_thermal_expansion
  ArrayList<String> mat_conc_mix_grad_results
  ArrayList<String> mat_soil_tests
  ArrayList<String> mat_unbound_gradations
  ArrayList<String> mat_unbound_tube_suction
  ArrayList<String> mat_soil_mr_results
  LinkedHashMap<String,String> asphalt
  LinkedHashMap<String,String> concrete
  LinkedHashMap<String,String> aggregateSoil
  LinkedHashMap<String,String> materialTestsFileMap

  String toString() {
    "${rdrive}\n${dataProductDataFolder}\n${materialSamplesFolder }\n${overwriteMatSamplesFiles}\n${tableName}\n${fileName}\n${countQuery}\n${mat_samples_columns}\n${selectPhrase}\n${querySuffix}\n${queryOrder}\n${wherePhrase}\n${testQueryOrder}\n${asphaltFolder}\n${concreteFolder}\n${aggregateSoilFolder}\n${mat_binder_abcd_test}\n${mat_binder_bbr_test}\n${mat_binder_critical_crack_temp}\n${mat_binder_dent_fracture}\n${mat_binder_dilatometr_tst}\n${mat_binder_dsr_tests}\n${mat_binder_dt_test}\n${mat_binder_fatigue}\n${mat_binder_repeated_creep}\n${mat_binder_strain_sweeps}\n${mat_binder_trad_tests}\n${mat_core_lengths}\n${mat_hma_aging}\n${mat_hma_apa}\n${mat_hma_bbr_test}\n${mat_hma_complex_shear_modu}\n${mat_hma_core_tests}\n${mat_hma_dct_test}\n${mat_hma_dilatometric_test}\n${mat_hma_dynamic_modulus}\n${mat_hma_flow_number}\n${mat_hma_hamburg}\n${mat_hma_idt_test}\n${mat_hma_indirect_tens_fati}\n${mat_hma_mix_tests}\n${mat_hma_original_density_air}\n${mat_hma_repeat_perm_deform}\n${mat_hma_repeat_shear}\n${mat_hma_scb_test}\n${mat_hma_senb_test}\n${mat_hma_sieve_data}\n${mat_hma_triaxial_static_creep}\n${mat_hma_triaxial_strength}\n${mat_hma_tsrst_test}\n${mat_hma_tti_overlay}\n${mat_hma_ultrasonic_modulus}\n${mat_conc_air_void_results}\n${mat_conc_field_results}\n${mat_conc_flex_strength}\n${mat_conc_freeze_thaw_results}\n${mat_conc_mod_poisson_results}\n${mat_conc_rapid_cloride}\n${mat_conc_strength_results}\n${mat_conc_thermal_expansion}\n${mat_conc_mix_grad_results}\n${mat_soil_tests}\n${mat_unbound_gradations}\n${mat_unbound_tube_suction}\n${mat_soil_mr_results}\n${asphalt}\n${concrete}\n${aggregateSoil}\n${materialTestsFileMap}"
  }
}
