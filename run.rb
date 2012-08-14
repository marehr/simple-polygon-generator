#!/usr/bin/env ruby
require 'open3'

File.delete(ARGV[1]) if File.exists?(ARGV[1])
400.times do |i|
  if i == 0
    `java -jar polygonsSWP-bin.jar --algorithm #{ARGV[0]} --threads 8 --number 600 --points #{i+4} >> #{ARGV[1]}`
  else
    Open3.popen2("java -jar polygonsSWP-bin.jar --algorithm #{ARGV[0]} --threads 8 --number 600 --points #{i+4}") do |stdin, stdout, thr|
      stdout.each do |l|
        unless l.start_with?("polygon")
          File.open(ARGV[1],"a").write(l)
        end
      end
    end
  end
end
