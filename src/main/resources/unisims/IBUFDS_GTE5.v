///////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 1995/2020 Xilinx, Inc.
//  All Right Reserved.
///////////////////////////////////////////////////////////////////////////////
//   ____  ____
//  /   /\/   /
// /___/  \  /     Vendor      : Xilinx
// \   \   \/      Version     : 2020.2
//  \   \          Description : Xilinx Unified Simulation Library Component
//  /   /                        IBUFDS_GTE5
// /___/   /\      Filename    : IBUFDS_GTE5.v
// \   \  /  \
//  \___\/\___\
//
///////////////////////////////////////////////////////////////////////////////
//  Revision:
//
//  End Revision:
///////////////////////////////////////////////////////////////////////////////

`timescale 1 ps / 1 ps

`celldefine

module IBUFDS_GTE5 #(
`ifdef XIL_TIMING
  parameter LOC = "UNPLACED",
`endif
  parameter [0:0] REFCLK_EN_TX_PATH = 1'b0,
  parameter integer REFCLK_HROW_CK_SEL = 0,
  parameter integer REFCLK_ICNTL_RX = 0
)(
  output O,
  output ODIV2,

  input CEB,
  input I,
  input IB
);

// define constants
  localparam MODULE_NAME = "IBUFDS_GTE5";
  
  reg trig_attr;
// include dynamic registers - XILINX test only
`ifdef XIL_DR
  `include "IBUFDS_GTE5_dr.v"
`else
  reg [0:0] REFCLK_EN_TX_PATH_REG = REFCLK_EN_TX_PATH;
  reg [31:0] REFCLK_HROW_CK_SEL_REG = REFCLK_HROW_CK_SEL;
  reg [31:0] REFCLK_ICNTL_RX_REG = REFCLK_ICNTL_RX;
`endif

`ifdef XIL_XECLIB
  wire [1:0] REFCLK_HROW_CK_SEL_BIN;
  wire [1:0] REFCLK_ICNTL_RX_BIN;
`else
  reg [1:0] REFCLK_HROW_CK_SEL_BIN;
  reg [1:0] REFCLK_ICNTL_RX_BIN;
`endif

`ifdef XIL_XECLIB
reg glblGSR = 1'b0;
`else
tri0 glblGSR = glbl.GSR;
`endif

`ifndef XIL_XECLIB
  reg attr_test;
  reg attr_err;
  
  initial begin
  trig_attr = 1'b0;
  `ifdef XIL_ATTR_TEST
    attr_test = 1'b1;
  `else
    attr_test = 1'b0;
  `endif
    attr_err = 1'b0;
    #1;
    trig_attr = ~trig_attr;
  end
`endif

`ifdef XIL_XECLIB
  assign REFCLK_HROW_CK_SEL_BIN = REFCLK_HROW_CK_SEL_REG[1:0];
  
  assign REFCLK_ICNTL_RX_BIN = REFCLK_ICNTL_RX_REG[1:0];
  
`else
  always @ (trig_attr) begin
  #1;
  REFCLK_HROW_CK_SEL_BIN = REFCLK_HROW_CK_SEL_REG[1:0];
  
  REFCLK_ICNTL_RX_BIN = REFCLK_ICNTL_RX_REG[1:0];
  
  end
`endif

`ifndef XIL_XECLIB
  always @ (trig_attr) begin
    #1;
    if ((attr_test == 1'b1) ||
        ((REFCLK_HROW_CK_SEL_REG != 0) &&
         (REFCLK_HROW_CK_SEL_REG != 1) &&
         (REFCLK_HROW_CK_SEL_REG != 2) &&
         (REFCLK_HROW_CK_SEL_REG != 3))) begin
      $display("Error: [Unisim %s-102] REFCLK_HROW_CK_SEL attribute is set to %d.  Legal values for this attribute are 0, 1, 2 or 3. Instance: %m", MODULE_NAME, REFCLK_HROW_CK_SEL_REG);
      attr_err = 1'b1;
    end
    
    if ((attr_test == 1'b1) ||
        ((REFCLK_ICNTL_RX_REG != 0) &&
         (REFCLK_ICNTL_RX_REG != 1) &&
         (REFCLK_ICNTL_RX_REG != 2) &&
         (REFCLK_ICNTL_RX_REG != 3))) begin
      $display("Error: [Unisim %s-103] REFCLK_ICNTL_RX attribute is set to %d.  Legal values for this attribute are 0, 1, 2 or 3. Instance: %m", MODULE_NAME, REFCLK_ICNTL_RX_REG);
      attr_err = 1'b1;
    end
    
    if (attr_err == 1'b1) #1 $finish;
  end
`endif

// begin behavioral model

  reg  ODIV2_out = 1'b0;
  assign ODIV2   = ODIV2_out;

  reg [2:0] ce_count = 3'b001;
  reg [2:0] edge_count = 3'b000;

  reg allEqual = 1'b0;

// =====================
// Count the rising edges of the clk
// =====================
  always @(posedge I or posedge glblGSR) begin
    if (allEqual || glblGSR)
      edge_count <= 3'b000;
    else
      if (CEB === 1'b0)
        edge_count <= edge_count + 1;
    end

//  Generate synchronous reset after DIVIDE number of counts
  always @(edge_count)
    if (edge_count == ce_count)
      allEqual = 1;
    else
      allEqual = 0;


  reg [1:0] edge_cnt = 2'b00;
  reg stop_cnt;
  always @(posedge I or posedge glblGSR) begin
    if (glblGSR) begin
      edge_cnt <= 3'b000;
      stop_cnt <= 1'b0;
    end
    else
      if (edge_cnt == 2'h2) begin
        edge_cnt <= 1'b0;
        stop_cnt <= 1'b1;
      end
      else if ((CEB === 1'b0) & !stop_cnt)
        edge_cnt <= edge_cnt + 1;
    end

  reg allEqual_p1 = 1'b0;
  always @(posedge I) begin
      allEqual_p1 = allEqual;
  end    

  wire allEqual_sum = (edge_cnt == 2'h1) | allEqual_p1;


// =====================
// Generate ODIV2
// =====================
  always @(*) begin
    case (REFCLK_HROW_CK_SEL_REG)
      2'b00: ODIV2_out <= ~(REFCLK_EN_TX_PATH_REG | (CEB === 1'b1)) && I;
      2'b01: ODIV2_out <= allEqual_sum;
      2'b10: ODIV2_out <= 1'b0;
      2'b11: ODIV2_out <= 1'b0;
      default : ODIV2_out <= ~(REFCLK_EN_TX_PATH_REG | (CEB === 1'b1)) && I;
    endcase
  end

// =====================
// Generate O
// =====================

  assign O = ~(REFCLK_EN_TX_PATH_REG | (CEB === 1'b1)) && I;


// end behavioral model

`ifndef XIL_XECLIB
`ifdef XIL_TIMING
  specify
    (I => O) = (0:0:0, 0:0:0);
    (I => ODIV2) = (0:0:0, 0:0:0);
    (IB => O) = (0:0:0, 0:0:0);
    (IB => ODIV2) = (0:0:0, 0:0:0);
    specparam PATHPULSE$ = 0;
  endspecify
`endif
`endif
endmodule

`endcelldefine
