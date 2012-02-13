require 'sqlite3'
require 'gnuplot'
require 'yaml'

db = SQLite3::Database.new( "test.db" )

def create_dataset(db_name, query)
  #fetch from db
  db = SQLite3::Database.new( db_name )
  rows = db.execute2(query)

  labels = Array.new
  cols = nil


  rows.each_with_index do |row, row_idx|
    if row_idx == 0
      cols = Array.new(row.size).map{|i| i = Array.new}
      labels = row
    else
      row.each_with_index{|value, col_idx| cols[col_idx][row_idx] = value}
    end
  end
  return [labels, cols]
end

def create_plot_file(plot_file, data_set, diagram_file, diagram_meta)

  File.open(plot_file, "w") do |gp|
    Gnuplot::Plot.new( gp ) do |plot|
    
      labels = data_set[0]
      cols = data_set[1]

      plot.output diagram_file
      plot.set("terminal", value = "latex")
      plot.title  diagram_meta["title"]
      plot.xlabel diagram_meta["xlable"]
      plot.ylabel diagram_meta["ylable"]
      
      plot.data = Array.new

      (cols.size-1).times do |i|
        plot.data[i] =
          Gnuplot::DataSet.new( [cols[0], cols[i+1]] ) do |ds|
            ds.with = "lines"
            ds.title = labels[i+1]
          end
      end
    end
  end
end

def main()  

  if ARGV.size() < 2
    puts `cat README`
    Kernel.exit(false)
  elsif ARGV[0] == "help"
    puts `cat README`
  end

  db_name = ARGV[0]
  query_type = ARGV[1]
  statistic_file = ARGV[2] != nil ? ARGV[2] : "statistic.tex"
  evaluations = YAML::load( File.open( 'evaluations.yaml' ) )

  if evaluations.keys.include?(query_type)
    evaluation = evaluations[query_type]
  else
    puts "evaluation not specified in evaluations.yaml"
  end

  statistic = ""
  diagrams = Array.new
  diagrams_string = ""

  # read temlate
  File.open("template.tex", 'r') do |file|  
    while line = file.gets
      statistic += line
    end
  end

  # set title of tex file
  statistic.gsub!(/!!title!!/, evaluation["doc_title"])

  # generate diagrams
  (evaluation.size-1).times do |i|
    diagram_name = "diagram" + (i+1).to_s
    data_set = create_dataset(db_name, evaluation[diagram_name]["query"])
    plot_file = "plot" + i.to_s
    diagram_file = "diagram" + i.to_s + ".tex"
    create_plot_file(plot_file, data_set, diagram_file, evaluation[diagram_name])
    `gnuplot #{plot_file}`
    diagrams[i] = diagram_file
  end

  # set diagrams as input for tex file
  diagrams.each do |diagram|
    diagrams_string += "\\input{" + diagram + "}\n \n"
  end
  statistic.gsub!(/!!diagrams!!/, diagrams_string)
  File.open(statistic_file, 'w').write(statistic)

end



main()
