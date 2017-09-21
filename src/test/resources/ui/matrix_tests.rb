require 'java'
require 'csv'
require 'time'

import 'org.datavyu.models.dataStore.legacy.Database'
import 'org.datavyu.models.dataStore.legacy.DataColumn'
import 'org.datavyu.models.dataStore.legacy.MatrixVocabElement'
import 'org.datavyu.models.dataStore.legacy.Matrix'
import 'org.datavyu.models.dataStore.legacy.FloatDataValue'
import 'org.datavyu.models.dataStore.legacy.IntDataValue'
import 'org.datavyu.models.dataStore.legacy.TextStringDataValue'
import 'org.datavyu.models.dataStore.legacy.QuoteStringDataValue'
import 'org.datavyu.models.dataStore.legacy.UndefinedDataValue'
import 'org.datavyu.models.dataStore.legacy.NominalDataValue'
import 'org.datavyu.models.dataStore.legacy.PredDataValue'
import 'org.datavyu.models.dataStore.legacy.Predicate'
import 'org.datavyu.models.dataStore.legacy.PredicateVocabElement'
import 'org.datavyu.models.dataStore.legacy.FloatFormalArg'
import 'org.datavyu.models.dataStore.legacy.IntFormalArg'
import 'org.datavyu.models.dataStore.legacy.NominalFormalArg'
import 'org.datavyu.models.dataStore.legacy.PredFormalArg'
import 'org.datavyu.models.dataStore.legacy.QuoteStringFormalArg'
import 'org.datavyu.models.dataStore.legacy.UnTypedFormalArg'
import 'org.datavyu.models.dataStore.legacy.DBElement'
import 'org.datavyu.models.dataStore.legacy.TimeStamp'
import 'org.datavyu.models.dataStore.legacy.DataCell'
import 'org.datavyu.models.dataStore.legacy.SystemErrorException'

begin

  numrows = 10

  # Create a data columns
  puts "Set up columns.."
  colnames = ["mN1", "mF1", "mI1", "mN2", "mF2", "mI2", "mM1", "mM2"]

  for cc in 0...colnames.length
    if !$dataStore.col_name_in_use(colnames[cc])
      col = DataColumn.new($dataStore, colnames[cc], MatrixVocabElement::MatrixType::MATRIX)
      $dataStore.add_column(col)
    end
  end

  ##  # 1. Check if matrix already defined
  #  mve1 = $dataStore.get_vocab_element("matrixText1")
  #  if mve1.get_num_formal_args() == 1
  #    # Setup structure of matrix column
  #    mve1 = MatrixVocabElement.new(mve1)
  #    mve1.delete_formal_arg(0)
  #
  #    farg = NominalFormalArg.new($dataStore, "<nominal>")
  #    mve1.append_formal_arg(farg)
  #    $dataStore.replace_matrix_ve(mve1)
  #  end
  #  matID1 = mve1.get_id()

  # 2. Check if matrix already defined
  mve1 = $dataStore.get_vocab_element("mN1")
  if mve1.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve1 = MatrixVocabElement.new(mve1)
    mve1.delete_formal_arg(0)
    farg = NominalFormalArg.new($dataStore, "<nominal>")
    mve1.append_formal_arg(farg)
    $dataStore.replace_matrix_ve(mve1)
  end
  matID1 = mve1.get_id()

  # 3. Check if matrix already defined
  mve3 = $dataStore.get_vocab_element("mF1")
  if mve3.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve3 = MatrixVocabElement.new(mve3)
    mve3.delete_formal_arg(0)

    farg = FloatFormalArg.new($dataStore, "<float>")
    mve3.append_formal_arg(farg)
    $dataStore.replace_matrix_ve(mve3)
  end
  matID3 = mve3.get_id()

  # 4. Check if matrix already defined
  mve4 = $dataStore.get_vocab_element("mI1")
  if mve4.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve4 = MatrixVocabElement.new(mve4)
    mve4.delete_formal_arg(0)

    farg = IntFormalArg.new($dataStore, "<int>")
    mve4.append_formal_arg(farg)
    $dataStore.replace_matrix_ve(mve4)
  end
  matID4 = mve4.get_id()

  #  # 5. Check if matrix already defined
  #  mve5 = $dataStore.get_vocab_element("matrixText2")
  #  if mve5.get_num_formal_args() == 1
  #  #  # Setup structure of matrix column
  #  #  mve5 = MatrixVocabElement.new(mve5)
  #  #  mve5.delete_formal_arg(0)
  #
  #  #  farg = TextFormalArg.new($dataStore, "<text>")
  #  #  mve5.append_formal_arg(farg)
  #  #  $dataStore.replace_matrix_ve(mve5)
  #  end
  #  matID5 = mve5.get_id()

  # 6. Check if matrix already defined
  mve6 = $dataStore.get_vocab_element("mN2")
  if mve6.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve6 = MatrixVocabElement.new(mve6)
    mve6.delete_formal_arg(0)
    farg = NominalFormalArg.new($dataStore, "<nominal1>")
    mve6.append_formal_arg(farg)
    farg = NominalFormalArg.new($dataStore, "<nominal2>")
    mve6.append_formal_arg(farg)
    $dataStore.replace_matrix_ve(mve6)
  end
  matID6 = mve6.get_id()

  # 7. Check if matrix already defined
  mve7 = $dataStore.get_vocab_element("mF2")
  if mve7.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve7 = MatrixVocabElement.new(mve7)
    mve7.delete_formal_arg(0)

    farg = FloatFormalArg.new($dataStore, "<float1>")
    mve7.append_formal_arg(farg)
    farg = FloatFormalArg.new($dataStore, "<float2>")
    mve7.append_formal_arg(farg)
    $dataStore.replace_matrix_ve(mve7)
  end
  matID7 = mve7.get_id()

  # 8. Check if matrix already defined
  mve8 = $dataStore.get_vocab_element("mI2")
  if mve8.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve8 = MatrixVocabElement.new(mve8)
    mve8.delete_formal_arg(0)

    farg = IntFormalArg.new($dataStore, "<int1>")
    mve8.append_formal_arg(farg)
    farg = IntFormalArg.new($dataStore, "<int2>")
    mve8.append_formal_arg(farg)
    $dataStore.replace_matrix_ve(mve8)
  end
  matID8 = mve8.get_id()

  # 9. Check if matrix already defined
  mve9 = $dataStore.get_vocab_element("mM1")
  if mve9.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve9 = MatrixVocabElement.new(mve9)
    mve9.delete_formal_arg(0)

    farg = FloatFormalArg.new($dataStore, "<float>")
    mve9.append_formal_arg(farg)
    farg = IntFormalArg.new($dataStore, "<int>")
    mve9.append_formal_arg(farg)
    farg = NominalFormalArg.new($dataStore, "<nominal>")
    mve9.append_formal_arg(farg)
    farg = QuoteStringFormalArg.new($dataStore, "<text>") # My changes
    mve9.append_formal_arg(farg)			       # ..
    $dataStore.replace_matrix_ve(mve9)
  end
  matID9 = mve9.get_id()

  # 10. Check if matrix already defined
  mve0 = $dataStore.get_vocab_element("mM2")
  if mve0.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve0 = MatrixVocabElement.new(mve0)
    mve0.delete_formal_arg(0)

    #        farg = QuoteStringFormalArg.new($dataStore, "<text>")
    #    mve0.append_formal_arg(farg)
    farg = FloatFormalArg.new($dataStore, "<float1>")
    mve0.append_formal_arg(farg)
    farg = IntFormalArg.new($dataStore, "<int1>")
    mve0.append_formal_arg(farg)
    farg = IntFormalArg.new($dataStore, "<int2>")
    mve0.append_formal_arg(farg)
    farg = NominalFormalArg.new($dataStore, "<nominal1>")
    mve0.append_formal_arg(farg)
    farg = FloatFormalArg.new($dataStore, "<float2>")
    mve0.append_formal_arg(farg)
    #    farg = QuoteStringFormalArg.new($dataStore, "<text>")
    #    mve0.append_formal_arg(farg)
    farg = NominalFormalArg.new($dataStore, "<nominal2>")
    mve0.append_formal_arg(farg)
    $dataStore.replace_matrix_ve(mve0)
  end
  matID0 = mve0.get_id()

  puts "Finished"

rescue NativeException => e
  puts "Datavyu Exception: '" + e + "'"
end


