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
  colnames = ["float", "int", "text", "nominal", "predicate", "matrix"]
  coltypes = [
    MatrixVocabElement::MatrixType::FLOAT,
    MatrixVocabElement::MatrixType::INTEGER,
    MatrixVocabElement::MatrixType::TEXT,
    MatrixVocabElement::MatrixType::NOMINAL,
    MatrixVocabElement::MatrixType::PREDICATE,
    MatrixVocabElement::MatrixType::MATRIX ]

  for cc in 0...colnames.length
    if !$dataStore.col_name_in_use(colnames[cc])
      col = DataColumn.new($dataStore, colnames[cc], coltypes[cc])
      $dataStore.add_column(col)
    end
  end

  # Check if predicate already defined
  if !$dataStore.pred_name_in_use("test0")
    # Make demo Predicate structure
    pve0 = PredicateVocabElement.new($dataStore, "test0");
    farg = FloatFormalArg.new($dataStore, "<float>")
    pve0.append_formal_arg(farg)
    farg = IntFormalArg.new($dataStore, "<int>")
    pve0.append_formal_arg(farg)
    farg = NominalFormalArg.new($dataStore, "<nominal>")
    pve0.append_formal_arg(farg)
    farg = QuoteStringFormalArg.new($dataStore, "<qstring>")
    pve0.append_formal_arg(farg)
    predID0 = $dataStore.add_pred_ve(pve0)
  end
  predID0 = $dataStore.get_pred_ve("test0").get_id()

  # Check if matrix already defined
  mve0 = $dataStore.get_vocab_element("matrix")
  if mve0.get_num_formal_args() == 1
    # Setup structure of matrix column
    mve0 = MatrixVocabElement.new(mve0)
    mve0.delete_formal_arg(0)

    farg = FloatFormalArg.new($dataStore, "<float>")
    mve0.append_formal_arg(farg)
    farg = IntFormalArg.new($dataStore, "<int>")
    mve0.append_formal_arg(farg)
    farg = NominalFormalArg.new($dataStore, "<nominal>")
    mve0.append_formal_arg(farg)
    farg = QuoteStringFormalArg.new($dataStore, "<qstring>")
    mve0.append_formal_arg(farg)
    $dataStore.replace_matrix_ve(mve0)
  end
  matID0 = mve0.get_id()

  # Create some data
  coldata = [0.1234, 1234, "textdata", "nom1"]
  col1 = [0, 2, 3, 10, 10, 15, 20, 22, 30, 34, 33, 35, 35, 50, 52, 53, 54, 55, 56, 57]
  col2 = [0, 20, 30, 31, 32, 38, 46, 51, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62]
  col3 = [0, 4, 5, 10, 11, 15, 17, 22, 24, 34, 34, 35, 35, 50, 52, 53, 54, 55, 56, 57]
  col4 = [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38]
  specialstring = "moo"
  for cc in 0...colnames.length
    col = $dataStore.get_column(colnames[cc])
    mve = $dataStore.get_matrix_ve(col.its_mve_id)

    for dd in 0...numrows
      cell = DataCell.new($dataStore, col.get_id, mve.get_id)
#      onset = cc * 1000 + (cc + dd) * 2000
#      offset = onset + (dd * 200)

      # Set different data cellValues
      if coltypes[cc] == MatrixVocabElement::MatrixType::FLOAT
        dv = FloatDataValue.new($dataStore)
        dv.set_its_value(coldata[cc] * dd)
        cell.onset = TimeStamp.new(1000, col1[dd * 2] * 1000)
        cell.offset = TimeStamp.new(1000, col1[dd * 2 + 1] * 1000)
      elsif coltypes[cc] == MatrixVocabElement::MatrixType::INTEGER
        dv = IntDataValue.new($dataStore)
        dv.set_its_value(coldata[cc] * dd)
        cell.onset = TimeStamp.new(1000, col2[dd * 2] * 1000)
        cell.offset = TimeStamp.new(1000, col2[dd * 2 + 1] * 1000)
      elsif coltypes[cc] == MatrixVocabElement::MatrixType::TEXT
        dv = TextStringDataValue.new($dataStore)
		if dd % 2 == 0 
		 dv.set_its_value(coldata[cc] + dd.to_s())
		else
		 dv.set_its_value(specialstring + dd.to_s())
		end
        cell.onset = TimeStamp.new(1000, col3[dd * 2] * 1000)
        cell.offset = TimeStamp.new(1000, col3[dd * 2 + 1] * 1000)
      elsif coltypes[cc] == MatrixVocabElement::MatrixType::NOMINAL
        dv = NominalDataValue.new($dataStore)
        dv.set_its_value(coldata[cc])
        cell.onset = TimeStamp.new(1000, col4[dd * 2] * 1000)
        cell.offset = TimeStamp.new(1000, col4[dd * 2 + 1] * 1000)
      elsif coltypes[cc] == MatrixVocabElement::MatrixType::PREDICATE
        cell.onset = TimeStamp.new(1000, col1[dd * 2] * 1000)
        cell.offset = TimeStamp.new(1000, col1[dd * 2 + 1] * 1000)
        pve0 = $dataStore.get_pred_ve(predID0)

        fargid = pve0.get_formal_arg(0).get_id()
        fdv0 = FloatDataValue.new($dataStore, fargid, 1.234)
        fargid = pve0.get_formal_arg(1).get_id()
        fdv1 = IntDataValue.new($dataStore, fargid, 1234)
        fargid = pve0.get_formal_arg(2).get_id()
        fdv2 = NominalDataValue.new($dataStore, fargid, "a_nominal")
        fargid = pve0.get_formal_arg(3).get_id()
        fdv3 = QuoteStringDataValue.new($dataStore, fargid, "quote_string")

        if dd == 0
          # construct a predicate with null args in first cell of column
          pred = Predicate.new($dataStore, predID0)
        else
          pred = Predicate.new(Predicate.construct($dataStore, predID0, fdv0, fdv1, fdv2, fdv3))
        end

        dv = PredDataValue.new($dataStore)
        dv.set_its_value(pred)

      elsif coltypes[cc] == MatrixVocabElement::MatrixType::MATRIX
        cell.onset = TimeStamp.new(1000, col2[dd * 2] * 1000)
        cell.offset = TimeStamp.new(1000, col2[dd * 2 + 1] * 1000)
        mve0 = $dataStore.get_matrix_ve(matID0)

        fargid = mve0.get_formal_arg(0).get_id()
        fdv0 = FloatDataValue.new($dataStore, fargid, 1.2)
        fargid = mve0.get_formal_arg(1).get_id()
        fdv1 = IntDataValue.new($dataStore, fargid, 4)
        fargid = mve0.get_formal_arg(2).get_id()
        fdv2 = NominalDataValue.new($dataStore, fargid, "nm")
        fargid = mve0.get_formal_arg(3).get_id()
        fdv3 = QuoteStringDataValue.new($dataStore, fargid, "qs")

        if dd == 0
          # construct a matrix with null args in first cell of column
          mat = Matrix.new($dataStore, matID0)
        else
          mat = Matrix.new(Matrix.construct($dataStore, matID0, fdv0, fdv1, fdv2, fdv3))
        end
      end

      # the ones that are only datavalues need to be put in a 1 arg matrix
      if coltypes[cc] != MatrixVocabElement::MatrixType::MATRIX
        mat = Matrix.new(Matrix.construct($dataStore, mve.get_id, dv))
      end

      # set the cell cellValue
      cell.set_val(mat)

      # Add the cell to the database.
      $dataStore.append_cell(cell)

    end
  end

  puts "Finished"

rescue NativeException => e
    puts "Datavyu Exception: '" + e + "'"
end


